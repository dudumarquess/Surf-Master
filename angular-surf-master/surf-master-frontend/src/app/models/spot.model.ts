export interface Spot {
  id: number;
  name: string;
  longitude: number;
  latitude: number;
  swellBestDirection: string | null;
  windBestDirection: string | null;
  recommendedLevel: string | null;
  notes: string[] | null;
}
