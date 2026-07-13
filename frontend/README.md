# Angular 20 Microfrontend Architecture

This project implements a microfrontend architecture using Angular 20 and Module Federation.

## Architecture

- **main-web** (Host Application - Port 4200)
  - Login & Registration UI
  - Home dashboard with privilege-based menu
  - Environment switcher (Production/Sandbox)
  - Loads qris-web and cashin-web as microfrontends

- **qris-web** (Remote Application - Port 4201)
  - QRIS transaction list
  - QRIS code generation
  - Shows "dummy" badge in sandbox mode

- **cashin-web** (Remote Application - Port 4202)
  - Virtual Account list
  - Top Up transaction list

## Technology Stack

- Angular 20
- Module Federation (@angular-architects/module-federation)
- Standalone Components
- SCSS for styling
- RxJS for state management

## API Gateway

- Production: http://localhost:8080/api/*
- Sandbox: http://localhost:8085/api/*

## Running the Applications

### Install dependencies
```bash
cd frontend/main-web && npm install
cd ../qris-web && npm install
cd ../cashin-web && npm install
```

### Start applications
```bash
# Terminal 1 - Start main-web (host)
cd frontend/main-web
npm start

# Terminal 2 - Start qris-web (remote)
cd frontend/qris-web
npm start

# Terminal 3 - Start cashin-web (remote)
cd frontend/cashin-web
npm start
```

### Access the application
Navigate to http://localhost:4200

## Features

- JWT authentication with localStorage
- Automatic Authorization header injection
- Environment switching between Production and Sandbox
- Privilege-based navigation menu
- Responsive design with dark theme

## Module Federation Configuration

### main-web (Host)
```typescript
remotes: {
  qrisWeb: 'http://localhost:4201/remoteEntry.js',
  cashinWeb: 'http://localhost:4202/remoteEntry.js',
}
```

### qris-web & cashin-web (Remotes)
Each exposes its module for lazy loading:
- qris-web exposes: `QrisModule`
- cashin-web exposes: `CashInModule`
