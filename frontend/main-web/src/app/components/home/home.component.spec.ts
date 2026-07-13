import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { HomeComponent } from './home.component';
import { AuthService } from '../../services/auth.service';
import { EnvironmentService } from '../../services/environment.service';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let envService: jasmine.SpyObj<EnvironmentService>;

  beforeEach(() => {
    const authSpy = jasmine.createSpyObj('AuthService', ['logout', 'isLoggedIn']);
    const envSpy = jasmine.createSpyObj('EnvironmentService', ['setEnvironment'], {
      environment: 'production',
      apiUrl: 'http://localhost:8080'
    });

    TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [
        { provide: AuthService, useValue: authSpy },
        { provide: EnvironmentService, useValue: envSpy }
      ]
    });

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    envService = TestBed.inject(EnvironmentService) as jasmine.SpyObj<EnvironmentService>;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should display welcome message', () => {
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('h1').textContent).toContain('Welcome to Sandbox');
  });

  it('should display logout button', () => {
    const compiled = fixture.nativeElement;
    const logoutButton = compiled.querySelector('.header button');
    expect(logoutButton).toBeTruthy();
    expect(logoutButton.textContent).toContain('Logout');
  });

  it('should display environment switcher with select', () => {
    const compiled = fixture.nativeElement;
    const label = compiled.querySelector('.env-switcher label');
    expect(label.textContent).toContain('Environment');

    const select = compiled.querySelector('.env-switcher select');
    expect(select).toBeTruthy();
  });

  it('should display logged in message', () => {
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('.content p').textContent).toContain('You are logged in!');
  });

  it('should initialize selectedEnv from EnvironmentService', () => {
    expect(component.selectedEnv).toBe('production');
  });

  it('should display current API URL', () => {
    const compiled = fixture.nativeElement;
    const apiUrlElement = compiled.querySelector('.env-switcher p');
    expect(apiUrlElement.textContent).toContain('http://localhost:8080');
  });

  it('should return apiUrl from EnvironmentService', () => {
    expect(component.apiUrl).toBe('http://localhost:8080');
  });

  it('should call authService.logout when logout button is clicked', () => {
    spyOn(component, 'onLogout').and.returnValue();
    const logoutButton = fixture.debugElement.query(By.css('.header button'));
    logoutButton.triggerEventHandler('click', null);
    expect(component.onLogout).toHaveBeenCalled();
  });

  it('should call envService.setEnvironment on environment change', () => {
    component.selectedEnv = 'sandbox';
    component.onEnvChange();
    expect(envService.setEnvironment).toHaveBeenCalledWith('sandbox');
  });

  it('should have production and sandbox options in the select', () => {
    const options = fixture.debugElement.queryAll(By.css('.env-switcher select option'));
    expect(options.length).toBe(2);
    expect(options[0].nativeElement.value).toBe('production');
    expect(options[1].nativeElement.value).toBe('sandbox');
  });

  it('should show production selected by default', async () => {
    const select = fixture.debugElement.query(By.css('.env-switcher select'));
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    expect(select.nativeElement.value).toBe('production');
  });
});
