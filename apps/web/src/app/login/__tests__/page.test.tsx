import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import LoginPage from '../page';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'next/navigation';

// Mock dependencies
jest.mock('next/navigation', () => ({
  useRouter: jest.fn(),
}));

jest.mock('@/stores/auth', () => ({
  useAuthStore: jest.fn(),
}));

jest.mock('react-hot-toast', () => ({
  __esModule: true,
  default: {
    success: jest.fn(),
    error: jest.fn(),
  },
  Toaster: () => <div>Toaster</div>,
}));

describe('LoginPage', () => {
  const mockPush = jest.fn();
  const mockReplace = jest.fn();
  const mockLogin = jest.fn();
  const mockCheckAuth = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useRouter as jest.Mock).mockReturnValue({
      push: mockPush,
      replace: mockReplace,
    });
    (useAuthStore as unknown as jest.Mock).mockReturnValue({
      login: mockLogin,
      isAuthenticated: false,
      checkAuth: mockCheckAuth,
    });
  });

  it('should render login form', () => {
    render(<LoginPage />);

    expect(screen.getByText('BreadDesk')).toBeInTheDocument();
    expect(screen.getByLabelText('이메일')).toBeInTheDocument();
    expect(screen.getByLabelText('비밀번호')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '로그인' })).toBeInTheDocument();
  });

  it('should render demo account button', () => {
    render(<LoginPage />);

    expect(screen.getByText('데모 계정으로 체험하기')).toBeInTheDocument();
  });

  it('should fill demo credentials when demo button is clicked', async () => {
    const user = userEvent.setup();
    render(<LoginPage />);

    const emailInput = screen.getByLabelText('이메일') as HTMLInputElement;
    const passwordInput = screen.getByLabelText('비밀번호') as HTMLInputElement;
    const demoButton = screen.getByText('데모 계정으로 체험하기');

    // Initially empty
    expect(emailInput.value).toBe('');
    expect(passwordInput.value).toBe('');

    // Click demo button
    await user.click(demoButton);

    // Should fill credentials
    expect(emailInput.value).toBe('demo@breaddesk.com');
    expect(passwordInput.value).toBe('demo1234');
  });

  it('should call login with correct credentials on form submit', async () => {
    const user = userEvent.setup();
    mockLogin.mockResolvedValue(undefined);

    render(<LoginPage />);

    const emailInput = screen.getByLabelText('이메일');
    const passwordInput = screen.getByLabelText('비밀번호');
    const submitButton = screen.getByRole('button', { name: '로그인' });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123',
      });
    });
  });

  it('should show loading state during login', async () => {
    const user = userEvent.setup();
    mockLogin.mockImplementation(() => new Promise(() => {})); // Never resolves

    render(<LoginPage />);

    const emailInput = screen.getByLabelText('이메일');
    const passwordInput = screen.getByLabelText('비밀번호');
    const submitButton = screen.getByRole('button', { name: '로그인' });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('로그인 중...')).toBeInTheDocument();
      expect(submitButton).toBeDisabled();
    });
  });

  it('should redirect to dashboard after successful login', async () => {
    const user = userEvent.setup();
    mockLogin.mockResolvedValue(undefined);

    render(<LoginPage />);

    const emailInput = screen.getByLabelText('이메일');
    const passwordInput = screen.getByLabelText('비밀번호');
    const submitButton = screen.getByRole('button', { name: '로그인' });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockPush).toHaveBeenCalledWith('/dashboard');
    });
  });

  it('should show error message on login failure', async () => {
    const user = userEvent.setup();
    const errorMessage = '로그인 실패';
    mockLogin.mockRejectedValue(new Error(errorMessage));

    render(<LoginPage />);

    const emailInput = screen.getByLabelText('이메일');
    const passwordInput = screen.getByLabelText('비밀번호');
    const submitButton = screen.getByRole('button', { name: '로그인' });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  it('should redirect to dashboard if already authenticated', () => {
    (useAuthStore as unknown as jest.Mock).mockReturnValue({
      login: mockLogin,
      isAuthenticated: true,
      checkAuth: mockCheckAuth,
    });

    render(<LoginPage />);

    expect(mockReplace).toHaveBeenCalledWith('/dashboard');
  });

  it('should call checkAuth on mount', () => {
    render(<LoginPage />);

    expect(mockCheckAuth).toHaveBeenCalled();
  });

  it('should work with demo account credentials', async () => {
    const user = userEvent.setup();
    mockLogin.mockResolvedValue(undefined);

    render(<LoginPage />);

    const demoButton = screen.getByText('데모 계정으로 체험하기');
    const submitButton = screen.getByRole('button', { name: '로그인' });

    // Click demo button to fill credentials
    await user.click(demoButton);

    // Submit form
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith({
        email: 'demo@breaddesk.com',
        password: 'demo1234',
      });
    });
  });
});
