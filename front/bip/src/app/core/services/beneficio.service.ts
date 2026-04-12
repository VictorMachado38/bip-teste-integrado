import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Beneficio } from '../models/beneficio.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class BeneficioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/beneficios`;

  findAll(): Observable<Beneficio[]> {
    return this.http.get<Beneficio[]>(this.baseUrl);
  }

  create(beneficio: Pick<Beneficio, 'nome' | 'valor'>): Observable<Beneficio> {
    return this.http.post<Beneficio>(this.baseUrl, beneficio);
  }

  update(id: number, beneficio: Beneficio): Observable<Beneficio> {
    return this.http.put<Beneficio>(`${this.baseUrl}/${id}`, beneficio);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  transfer(fromId: number, toId: number, amount: number): Observable<void> {
    const params = new HttpParams()
      .set('fromId', fromId.toString())
      .set('toId', toId.toString())
      .set('amount', amount.toString());
    return this.http.post<void>(`${this.baseUrl}/transfer`, null, { params });
  }
}