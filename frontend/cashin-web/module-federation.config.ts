import { withModuleFederationPlugin } from '@angular-architects/module-federation';

export default withModuleFederationPlugin({
  name: 'cashinWeb',
  
  exposes: {
    './CashInModule': './src/app/cashin.module.ts',
  },
  
  shared: {
    '@angular/core': { singleton: true, strictVersion: true, requiredVersion: '^20.0.0' },
    '@angular/common': { singleton: true, strictVersion: true, requiredVersion: '^20.0.0' },
    '@angular/common/http': { singleton: true, strictVersion: true, requiredVersion: '^20.0.0' },
    '@angular/router': { singleton: true, strictVersion: true, requiredVersion: '^20.0.0' },
    '@angular/forms': { singleton: true, strictVersion: true, requiredVersion: '^20.0.0' },
  },
});
