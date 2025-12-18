import { Component, OnInit } from '@angular/core';

import { SpotService } from '../../services/spot.service';
import { ProfileService } from '../../services/profile.service';
import { Spot } from '../../models/spot.model';
import { UserProfile } from '../../models/profile.model';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  spots: Spot[] = [];
  loadingSpots = false;
  spotsError?: string;

  profiles: UserProfile[] = [];
  profilesLoading = false;
  profilesError?: string;
  profilesVisible = false;

  constructor(
    private spotService: SpotService,
    private profileService: ProfileService
  ) {}

  ngOnInit(): void {
    this.loadSpots();
  }

  trackSpot(_index: number, spot: Spot): number {
    return spot.id;
  }

  toggleProfiles(): void {
    this.profilesVisible = !this.profilesVisible;
    if (this.profilesVisible && !this.profiles.length && !this.profilesLoading) {
      this.fetchProfiles();
    }
  }

  private loadSpots(): void {
    this.loadingSpots = true;
    this.spotsError = undefined;
    this.spotService.getSpots().subscribe({
      next: (spots) => {
        this.spots = spots;
      },
      error: () => {
        this.spotsError = 'We could not load the surf spots right now. Please try again shortly.';
      },
      complete: () => {
        this.loadingSpots = false;
      }
    });
  }

  private fetchProfiles(): void {
    this.profilesLoading = true;
    this.profilesError = undefined;
    this.profileService.listProfiles().subscribe({
      next: (profiles) => {
        this.profiles = profiles;
      },
      error: () => {
        this.profilesError = 'Profiles are unavailable right now.';
      },
      complete: () => {
        this.profilesLoading = false;
      }
    });
  }
}
