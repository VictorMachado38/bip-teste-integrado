import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Beneficio } from '../../../core/models/beneficio.model';
import { BeneficioService } from '../../../core/services/beneficio.service';
import { KeycloakService } from '../../../core/services/keycloak.service';
import { ToastComponent } from '../../../shared/components/toast/toast.component';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-beneficio-list',
  standalone: true,
  imports: [FormsModule, ToastComponent],
  templateUrl: './beneficio-list.component.html',
  styleUrl: './beneficio-list.component.scss',
})
export class BeneficioListComponent implements OnInit {
  private readonly beneficioService = inject(BeneficioService);
  private readonly toastService = inject(ToastService);
  private readonly keycloakService = inject(KeycloakService);

  readonly username = signal(this.keycloakService.username ?? 'Usuário');
  readonly roles = signal(this.keycloakService.roles.filter(r => !r.startsWith('default')).join(', '));

  // State
  readonly beneficios = signal<Beneficio[]>([]);
  readonly loading = signal(false);
  readonly submitting = signal(false);

  // Modals
  readonly showFormModal = signal(false);
  readonly showTransferModal = signal(false);
  readonly showDeleteModal = signal(false);
  readonly editingBeneficio = signal<Beneficio | null>(null);
  readonly deletingId = signal<number | null>(null);

  // Form data
  formData = { nome: '', valor: 0 };

  // Transfer data
  transferData = { fromId: 0, toId: 0, amount: 0 };

  // Computed stats
  readonly totalValue = computed(() => this.beneficios().reduce((sum, b) => sum + Number(b.valor), 0));
  readonly maxValue = computed(() =>
    this.beneficios().length ? Math.max(...this.beneficios().map(b => Number(b.valor))) : 0,
  );
  readonly beneficioCount = computed(() => this.beneficios().length);

  get transferDestinations(): Beneficio[] {
    return this.beneficios().filter(b => b.id !== this.transferData.fromId);
  }

  ngOnInit(): void {
    this.loadBeneficios();
  }

  loadBeneficios(): void {
    this.loading.set(true);
    this.beneficioService.findAll().subscribe({
      next: data => {
        this.beneficios.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.toastService.error('Erro ao carregar benefícios');
        this.loading.set(false);
      },
    });
  }

  openCreateModal(): void {
    this.editingBeneficio.set(null);
    this.formData = { nome: '', valor: 0 };
    this.showFormModal.set(true);
  }

  openEditModal(beneficio: Beneficio): void {
    this.editingBeneficio.set(beneficio);
    this.formData = { nome: beneficio.nome, valor: Number(beneficio.valor) };
    this.showFormModal.set(true);
  }

  openTransferModal(from: Beneficio): void {
    this.transferData = { fromId: from.id!, toId: 0, amount: 0 };
    this.showTransferModal.set(true);
  }

  openDeleteModal(id: number): void {
    this.deletingId.set(id);
    this.showDeleteModal.set(true);
  }

  closeModals(): void {
    this.showFormModal.set(false);
    this.showTransferModal.set(false);
    this.showDeleteModal.set(false);
  }

  saveForm(): void {
    if (!this.formData.nome.trim() || Number(this.formData.valor) <= 0) {
      this.toastService.warning('Preencha todos os campos corretamente');
      return;
    }

    this.submitting.set(true);
    const editing = this.editingBeneficio();

    const obs = editing
      ? this.beneficioService.update(editing.id!, { ...editing, ...this.formData })
      : this.beneficioService.create(this.formData);

    obs.subscribe({
      next: () => {
        this.toastService.success(editing ? 'Benefício atualizado!' : 'Benefício criado!');
        this.closeModals();
        this.loadBeneficios();
        this.submitting.set(false);
      },
      error: () => {
        this.toastService.error('Erro ao salvar benefício');
        this.submitting.set(false);
      },
    });
  }

  confirmDelete(): void {
    const id = this.deletingId();
    if (!id) return;
    this.submitting.set(true);
    this.beneficioService.delete(id).subscribe({
      next: () => {
        this.toastService.success('Benefício excluído!');
        this.closeModals();
        this.loadBeneficios();
        this.submitting.set(false);
      },
      error: () => {
        this.toastService.error('Erro ao excluir benefício');
        this.submitting.set(false);
      },
    });
  }

  doTransfer(): void {
    const fromId = Number(this.transferData.fromId);
    const toId = Number(this.transferData.toId);
    const amount = Number(this.transferData.amount);

    if (!toId || !amount || amount <= 0) {
      this.toastService.warning('Preencha todos os campos da transferência');
      return;
    }

    this.submitting.set(true);
    this.beneficioService.transfer(fromId, toId, amount).subscribe({
      next: () => {
        this.toastService.success('Transferência realizada com sucesso!');
        this.closeModals();
        this.loadBeneficios();
        this.submitting.set(false);
      },
      error: (err: { error?: { message?: string } }) => {
        this.toastService.error(err?.error?.message ?? 'Erro ao realizar transferência');
        this.submitting.set(false);
      },
    });
  }

  logout(): void {
    this.keycloakService.logout();
  }

  getBeneficioName(id: number): string {
    return this.beneficios().find(b => b.id === id)?.nome ?? '';
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(value));
  }

  getAvatarColor(nome: string): string {
    const colors = ['#6366f1', '#8b5cf6', '#06b6d4', '#10b981', '#f59e0b', '#ef4444', '#ec4899'];
    const index = nome.charCodeAt(0) % colors.length;
    return colors[index];
  }
}