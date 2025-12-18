export interface UserProfile {
  id: number;
  displayName: string;
  level: string | null;
  preferredBoards: string[] | null;
}
