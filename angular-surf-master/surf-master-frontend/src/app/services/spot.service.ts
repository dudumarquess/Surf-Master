import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Spot } from '../models/spot.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SpotService {
  private readonly baseUrl = `${environment.apiBaseUrl}/spots`;

  constructor(private http: HttpClient) {}

  getSpots(): Observable<Spot[]> {
    return this.http.get<Spot[]>(this.baseUrl);
  }
}
