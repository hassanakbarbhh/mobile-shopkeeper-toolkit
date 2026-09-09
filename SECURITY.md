# Security and Configuration Setup

## Firebase Secrets
All sensitive Firebase configurations and Google API keys MUST be injected via environment variables.

1. Create a `.env` file in the root of the project.
2. Add your keys:
```
FIREBASE_WEB_CLIENT_ID=your_web_client_id_here
```
3. Do NOT commit `.env` or `google-services.json` to version control.

## Keystore
1. The app uses `debug.keystore` for local development.
2. For production, generate a release keystore and define its path and passwords in local secure environment variables. Do NOT commit the release keystore.

## Access Control
- The app owner is strictly verified via email.
- New users default to `BASIC_USER` and must be approved by the owner via the Access Control panel.
