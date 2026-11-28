# Auto-deploy on the server

The server polls GitHub every minute. When `master` has new commits, it
fast-forwards the checkout and rebuilds only what changed:

| Changed paths                          | Rebuilt / restarted |
|----------------------------------------|---------------------|
| `backend/**`                           | backend             |
| `frontend/**`                          | frontend            |
| `docker-compose.yml`, `.env.example`   | everything          |
| anything else (docs, `deploy/`)        | nothing             |

The new image is built before the old container is replaced, so a failing build
leaves the running version untouched. A failed commit is not retried; the next
push retries it together with whatever it changed. Logs go to the journal.

## One-time setup (Raspberry Pi 5, 64-bit OS)

```sh
git clone https://github.com/AtanasRaev/gym-tracker.git ~/gym-tracker
cd ~/gym-tracker
cp .env.example .env            # fill in real values
sudo usermod -aG docker "$USER" # then log out and back in

mkdir -p ~/.config/systemd/user
cp deploy/gym-tracker-deploy.{service,timer} ~/.config/systemd/user/
systemctl --user daemon-reload
systemctl --user enable --now gym-tracker-deploy.timer
sudo loginctl enable-linger "$USER"  # keep the timer running without a login session
```

The first run builds and starts everything; on a Pi 5 the backend build takes a
few minutes.

## Access

The app listens only on `127.0.0.1:${APP_PORT}` (default 3004) on the server.
Before the first start, check the port is free: `sudo ss -lntp 'sport = :3004'`.

- **SSH tunnel:** `ssh -N -L 13004:127.0.0.1:3004 atanas@homeserver.local`, then
  open http://localhost:13004. Browsers treat `localhost` as secure, so login
  works despite the `secure` session cookie.
- **Tailscale Serve:** `sudo tailscale serve --bg --https=<port> http://127.0.0.1:3004`.
  Pick a port that doesn't replace an existing route (`sudo tailscale serve status`).
  It serves HTTPS, so login works.

Plain `http://<server-ip>` won't work for login: the session cookie is HTTPS-only.

## Day to day

```sh
journalctl --user -u gym-tracker-deploy -f          # watch deploys
systemctl --user start gym-tracker-deploy.service   # deploy now
rm .deploy/failed                                   # retry a failed commit
```

Changing `.env` on the server is not a git change; apply it with
`docker compose up -d`.
