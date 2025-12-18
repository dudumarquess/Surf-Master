export interface Forecast {
  id: number;
  spotId: number;
  timestamp: string;
  swellHeight: number | null;
  swellPeriod: number | null;
  swellDirection: string | null;
  windSpeed: number | null;
  windDirection: string | null;
  tideHeight: number | null;
  waterTemperature: number | null;
  dataSource: string | null;
}
