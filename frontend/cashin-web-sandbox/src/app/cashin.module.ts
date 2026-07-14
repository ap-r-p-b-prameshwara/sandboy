import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { VaListComponent } from './components/va-list/va-list.component';
import { TransactionListComponent } from './components/transaction-list/transaction-list.component';

const routes: Routes = [
  { path: '', redirectTo: 'va', pathMatch: 'full' },
  { path: 'va', component: VaListComponent },
  { path: 'transactions', component: TransactionListComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CashInRoutingModule {}

@NgModule({
  declarations: [],
  imports: [CashInRoutingModule, VaListComponent, TransactionListComponent],
})
export class CashInModule {}
