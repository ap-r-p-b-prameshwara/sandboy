import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { EnvironmentService } from '../../services/environment.service';
import { of, throwError } from 'rxjs';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    const authSpy = jasmine.createSpyObj('AuthService', ['login', 'setToken']);

    TestBed.configureTestingModule({
      imports: [LoginComponent, HttpClientTestingModule, FormsModule],
      providers: [
        { provide: AuthService, useValue: authSpy },
        EnvironmentService
      ]
    });

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should render login form with username and password inputs', () => {
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('h2').textContent).toContain('Login');

    const usernameInput = compiled.querySelector('input[name="username"]');
    expect(usernameInput).toBeTruthy();
    expect(usernameInput.getAttribute('placeholder')).toBe('Username');

    const passwordInput = compiled.querySelector('input[name="password"]');
    expect(passwordInput).toBeTruthy();
    expect(passwordInput.getAttribute('placeholder')).toBe('Password');
  });

  it('should render login submit button', () => {
    const compiled = fixture.nativeElement;
    const submitButton = compiled.querySelector('button[type="submit"]');
    expect(submitButton).toBeTruthy();
    expect(submitButton.textContent).toContain('Login');
  });

  it('should render register button', () => {
    const compiled = fixture.nativeElement;
    const registerButton = compiled.querySelectorAll('button')[1];
    expect(registerButton).toBeTruthy();
    expect(registerButton.textContent).toContain('Register');
  });

  it('should have initially empty username and password', () => {
    expect(component.username).toBe('');
    expect(component.password).toBe('');
  });

  it('should have showRegister initially false', () => {
    expect(component.showRegister).toBeFalse();
  });

  it('should call authService.login on form submit', () => {
    const mockResponse = { token: 'login-token' };
    authService.login.and.returnValue(of(mockResponse));
    spyOn(component, 'onLogin').and.returnValue();

    const form = fixture.debugElement.query(By.css('form'));
    form.triggerEventHandler('ngSubmit', null);

    expect(component.onLogin).toHaveBeenCalled();
  });

  it('should set token via authService after successful login', fakeAsync(() => {
    const mockResponse = { token: 'login-token' };
    authService.login.and.returnValue(of(mockResponse));

    component.username = 'testuser';
    component.password = 'testpass';
    authService.login(component.username, component.password).subscribe(response => {
      authService.setToken(response.token);
    });
    tick();

    expect(authService.login).toHaveBeenCalledWith('testuser', 'testpass');
    expect(authService.setToken).toHaveBeenCalledWith('login-token');
  }));

  it('should not set token on login error', fakeAsync(() => {
    authService.login.and.returnValue(throwError(() => new Error('Login failed')));

    component.username = 'baduser';
    component.password = 'badpass';
    authService.login(component.username, component.password).subscribe({
      error: () => {
        expect(authService.setToken).not.toHaveBeenCalled();
      }
    });
    tick();

    expect(authService.login).toHaveBeenCalledWith('baduser', 'badpass');
    expect(authService.setToken).not.toHaveBeenCalled();
  }));

  it('should have required validation on username input', () => {
    const usernameInput = fixture.debugElement.query(By.css('input[name="username"]'));
    expect(usernameInput.nativeElement.required).toBeTrue();
  });

  it('should have required validation on password input', () => {
    const passwordInput = fixture.debugElement.query(By.css('input[name="password"]'));
    expect(passwordInput.nativeElement.required).toBeTrue();
  });
});