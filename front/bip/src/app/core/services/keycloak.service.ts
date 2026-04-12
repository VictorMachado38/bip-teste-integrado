import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { environment } from '../../../environments/environment';

const keycloak = new Keycloak({
  url: environment.keycloakUrl,
  realm: 'bip-realm',
  clientId: 'bip-frontend',
});

@Injectable({ providedIn: 'root' })
export class KeycloakService {
  private _keycloak = keycloak;

  async init(): Promise<boolean> {
    return this._keycloak.init({
      onLoad: 'login-required',
      checkLoginIframe: false,
    });
  }

  get token(): string | undefined {
    return this._keycloak.token;
  }

  get username(): string | undefined {
    return this._keycloak.tokenParsed?.['preferred_username'];
  }

  get roles(): string[] {
    return this._keycloak.tokenParsed?.['realm_access']?.roles ?? [];
  }

  hasRole(role: string): boolean {
    return this.roles.includes(role);
  }

  async getValidToken(): Promise<string> {
    await this._keycloak.updateToken(30);
    return this._keycloak.token ?? '';
  }

  logout(): void {
    this._keycloak.logout({ redirectUri: window.location.origin });
  }
}