import { ChangeDetectionStrategy, Component, ElementRef, computed, inject, viewChild } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../core/auth/auth.service';
import { Icon, IconName } from '../shared/icon';

interface NavLink {
  path: string;
  label: string;
  icon: IconName;
}

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Icon],
  templateUrl: './app-shell.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppShell {
  protected readonly auth = inject(AuthService);
  private readonly main = viewChild.required<ElementRef<HTMLElement>>('main');

  protected readonly links: readonly NavLink[] = [
    { path: '/library', label: 'Library', icon: 'library' },
    { path: '/search', label: 'Search', icon: 'search' },
  ];

  protected readonly initial = computed(() => (this.auth.username() ?? '?').charAt(0).toUpperCase());

  /**
   * A fragment link (`href="#main"`) would resolve against `<base href="/">` and navigate away
   * from the current route, so the skip link moves focus programmatically instead.
   */
  protected skipToContent(): void {
    this.main().nativeElement.focus();
  }
}
