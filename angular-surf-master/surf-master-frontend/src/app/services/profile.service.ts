import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { UserProfile } from '../models/profile.model';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly baseUrl = `${environment.apiBaseUrl}/profile`;

  constructor(private http: HttpClient) {}

  listProfiles(): Observable<UserProfile[]> {
    return this.http.get<UserProfile[]>(this.baseUrl);
  }
}
