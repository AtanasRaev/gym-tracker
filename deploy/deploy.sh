#!/usr/bin/env bash
# Pull-based deploy: fetch the deploy branch and rebuild only what changed.
# Run periodically (see deploy/gym-tracker-deploy.timer). Safe to run by hand.
set -euo pipefail

cd "$(dirname "$0")/.."
BRANCH="${DEPLOY_BRANCH:-master}"
STATE_DIR=".deploy"
COMPOSE=(${COMPOSE:-docker compose})

# Lock lives in RAM (/run/user/<uid>), so idle checks don't touch the disk.
exec 9>"${XDG_RUNTIME_DIR:-/tmp}/gym-tracker-deploy.lock"
flock -n 9 || { echo "Another deploy is running"; exit 0; }

deployed=$(cat "$STATE_DIR/deployed" 2>/dev/null || true)
failed=$(cat "$STATE_DIR/failed" 2>/dev/null || true)

# Read-only check first: ls-remote writes nothing, unlike fetch (FETCH_HEAD).
remote=$(git ls-remote origin "refs/heads/$BRANCH" | cut -f1)
[[ -n "$remote" ]] || { echo "Branch $BRANCH not found on origin" >&2; exit 1; }
[[ "$remote" == "$deployed" ]] && exit 0
# Don't rebuild a known-broken commit every minute; wait for the next push.
[[ "$remote" == "$failed" ]] && exit 0

mkdir -p "$STATE_DIR"
git fetch --quiet origin "$BRANCH"
target=$(git rev-parse "origin/$BRANCH")

git merge --ff-only --quiet "$target"

# Diff against the last successful deploy, so a failed deploy's changes are
# still picked up by the next one.
if [[ -z "$deployed" ]] || ! git cat-file -e "$deployed^{commit}" 2>/dev/null; then
    services=(all)
else
    changed=$(git diff --name-only "$deployed" "$target")
    services=()
    if grep -qE '^(docker-compose\.yml|\.env\.example)$' <<<"$changed"; then
        services=(all)
    else
        grep -q '^backend/' <<<"$changed" && services+=(backend)
        grep -q '^frontend/' <<<"$changed" && services+=(frontend)
    fi
fi

echo "Deploying ${target:0:7} (services: ${services[*]:-none})"
deploy() {
    if [[ "${services[*]:-}" == all ]]; then
        "${COMPOSE[@]}" up -d --build --remove-orphans
    elif (( ${#services[@]} )); then
        "${COMPOSE[@]}" up -d --build --no-deps "${services[@]}"
    fi
}

if deploy; then
    echo "$target" > "$STATE_DIR/deployed"
    rm -f "$STATE_DIR/failed"
    docker image prune -f > /dev/null 2>&1 || true
    echo "Deployed ${target:0:7}"
else
    echo "$target" > "$STATE_DIR/failed"
    echo "Deploy of ${target:0:7} failed; previous containers keep running" >&2
    exit 1
fi
