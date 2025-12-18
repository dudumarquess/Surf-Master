import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { Forecast } from '../models/forecast.model';

@Injectable({ providedIn: 'root' })
export class ForecastService {
  private readonly baseUrl = `${environment.apiBaseUrl}/forecasts`;

  constructor(private http: HttpClient) {}

  getForecastsForSpot(spotId: number, from?: string, to?: string): Observable<Forecast[]> {
    let params = new HttpParams();
    if (from) {
      params = params.set('from', from);
    }
    if (to) {
      params = params.set('to', to);
    }

    return this.http.get<Forecast[]>(`${this.baseUrl}/spot/${spotId}`, { params });
  }
}
