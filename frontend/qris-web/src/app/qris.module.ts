import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TransactionListComponent } from './components/transaction-list/transaction-list.component';
import { QrisGenerateComponent } from './components/qris-generate/qris-generate.component';

const routes: Routes = [
  { path: '', redirectTo: 'transactions', pathMatch: 'full' },
  { path: 'transactions', component: TransactionListComponent },
  { path: 'generate', component: QrisGenerateComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class QrisRoutingModule {}

@NgModule({
  declarations: [],
  imports: [QrisRoutingModule, TransactionListComponent, QrisGenerateComponent],
})
export class QrisModule {}
