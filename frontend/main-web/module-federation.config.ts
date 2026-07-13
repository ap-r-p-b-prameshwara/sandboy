import { withModuleFederationPlugin } from '@angular-architects/module-federation';

export default withModuleFederationPlugin({
  name: 'mainWeb',
  
  remotes: {
    qrisWeb: {
      type: 'module',
      entry: 'http://localhost:4201/remoteEntry.js',
    },
    cashinWeb: {
      type: 'module',
      entry: 'http://localhost:4202/remoteEntry.js',
    },
  },
  
  shared: {
    '@angular/core': { singleton: true, strictVersion: true, requiredVersion: '^20.0.0' },
    '@angular/common': { singleton: true, strictVersion: true, requiredVersion: '^20.0.0' },
    '@angular/common/http': { singleton: true, strictVersion: true, requiredVersion: '^20.0.0' },
    '@angular/router': { singleton: true, strictVersion: true, requiredVersion: '^20.0.0' },
    '@angular/forms': { singleton: true, strictVersion: true, requiredVersion: '^20.0.0' },
  },
});
