# BooleRules Showcase

A little web application to showcase some functionality of the [BooleRules](https://github.com/booleworks/boolerules)
rule engine.

<a href="https://www.logicng.org"><img src="https://github.com/booleworks/boolerules-showcase/blob/main/assets/screenshot.png?raw=true" alt="logo" width="800"></a>

⚠ This is a developer preview, pre-alpha, and not ready for production use ⚠

## Quick Start

Spinning up Redis, the BooleRules backend, and frontent is as easy as downloading the
[Docker Compose file](https://github.com/booleworks/boolerules/blob/main/compose.yaml)
and running the command `docker compose up`.  This should expose the Swagger API of the
backend on port 7070 and the frontend on port 3000. So a `localhost:3000` should give
you the demonstrator frontend.

## Setup

Make sure to install the dependencies:

```bash
# yarn
yarn install

# npm
npm install

# pnpm
pnpm install
```

## Development Server

Start the development server on `http://localhost:3000`

```bash
npm run dev
```

## Production

Build the application for production:

```bash
npm run build
```

Locally preview production build:

```bash
npm run preview
```

Check out the [deployment documentation](https://nuxt.com/docs/getting-started/deployment) for more information.

## Funding

BooleRules development is funded by the [SofDCar project](https://sofdcar.de/):

<a href="https://www.logicng.org"><img src="https://github.com/booleworks/logicng-rs/blob/main/doc/logos/bmwk.png?raw=true" alt="logo" width="200"></a>

