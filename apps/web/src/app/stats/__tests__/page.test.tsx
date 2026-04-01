import { render, screen, waitFor } from '@testing-library/react';
import StatsPage from '../page';
import * as statsService from '@/services/stats';
import type { StatsOverview, AIStats, TeamStats, WeeklyReport } from '@/types';

// Mock the stats service
jest.mock('@/services/stats');

// Mock UI components
jest.mock('@/components/ui/LoadingSpinner', () => ({
  LoadingSpinner: () => <div>Loading...</div>,
}));

jest.mock('@/components/ui/ErrorMessage', () => ({
  ErrorMessage: ({ message, onRetry }: { message: string; onRetry: () => void }) => (
    <div>
      <div>Error: {message}</div>
      <button onClick={onRetry}>Retry</button>
    </div>
  ),
}));

describe('StatsPage', () => {
  const mockOverview: StatsOverview = {
    totalInquiries: 100,
    totalTasks: 50,
    totalMembers: 5,
    aiResolutionRate: 0.75,
    avgResponseTime: 15.5,
    avgResolveTime: 45.2,
  };

  const mockAIStats: AIStats = {
    totalAIAnswered: 75,
    autoResolvedCount: 60,
    autoResolvedRate: 0.8,
    escalatedCount: 15,
    escalatedRate: 0.2,
    confidenceDistribution: {
      'HIGH(0.8+)': 40,
      'MEDIUM(0.5-0.8)': 25,
      'LOW(<0.5)': 10,
    },
  };

  const mockTeamStats: TeamStats = [
    {
      memberId: 1,
      memberName: 'John Doe',
      assignedCount: 20,
      completedCount: 15,
      avgProcessingTimeHours: 3.5,
    },
    {
      memberId: 2,
      memberName: 'Jane Smith',
      assignedCount: 15,
      completedCount: 12,
      avgProcessingTimeHours: 2.8,
    },
  ];

  const mockWeeklyReport: WeeklyReport = {
    weekStart: '2026-03-24',
    weekEnd: '2026-03-30',
    newInquiries: 85,
    resolvedInquiries: 70,
    newTasks: 40,
    completedTasks: 35,
    aiResolutionRate: 0.7,
    slaComplianceRate: 0.9,
    topPerformers: [
      { memberName: 'John Doe', completedCount: 15 },
      { memberName: 'Jane Smith', completedCount: 12 },
    ],
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should show loading state initially', () => {
    (statsService.getStatsOverview as jest.Mock).mockReturnValue(new Promise(() => {}));
    (statsService.getAIStats as jest.Mock).mockReturnValue(new Promise(() => {}));
    (statsService.getTeamStats as jest.Mock).mockReturnValue(new Promise(() => {}));
    (statsService.getWeeklyReport as jest.Mock).mockReturnValue(new Promise(() => {}));

    render(<StatsPage />);
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('should render all stats sections when data loads successfully', async () => {
    (statsService.getStatsOverview as jest.Mock).mockResolvedValue(mockOverview);
    (statsService.getAIStats as jest.Mock).mockResolvedValue(mockAIStats);
    (statsService.getTeamStats as jest.Mock).mockResolvedValue(mockTeamStats);
    (statsService.getWeeklyReport as jest.Mock).mockResolvedValue(mockWeeklyReport);

    render(<StatsPage />);

    await waitFor(() => {
      expect(screen.getByText('📈 통계')).toBeInTheDocument();
    });

    // Overview stats
    expect(screen.getByText('전체 문의')).toBeInTheDocument();
    expect(screen.getByText('100')).toBeInTheDocument();
    expect(screen.getByText('75.0%')).toBeInTheDocument(); // AI resolution rate

    // AI Stats
    expect(screen.getByText('🤖 AI 성과')).toBeInTheDocument();
    expect(screen.getByText('75')).toBeInTheDocument(); // totalAIAnswered
    expect(screen.getByText('80.0%')).toBeInTheDocument(); // autoResolvedRate

    // Team Stats
    expect(screen.getByText('👥 팀별 성과')).toBeInTheDocument();
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('Jane Smith')).toBeInTheDocument();

    // Weekly Report
    expect(screen.getByText('📅 주간 리포트')).toBeInTheDocument();
    expect(screen.getByText('2026-03-24 ~ 2026-03-30')).toBeInTheDocument();
  });

  it('should handle API error gracefully', async () => {
    const errorMessage = 'Failed to fetch stats';
    (statsService.getStatsOverview as jest.Mock).mockRejectedValue(new Error(errorMessage));
    (statsService.getAIStats as jest.Mock).mockRejectedValue(new Error(errorMessage));
    (statsService.getTeamStats as jest.Mock).mockRejectedValue(new Error(errorMessage));
    (statsService.getWeeklyReport as jest.Mock).mockRejectedValue(new Error(errorMessage));

    render(<StatsPage />);

    await waitFor(() => {
      expect(screen.getByText(`Error: ${errorMessage}`)).toBeInTheDocument();
    });
  });

  it('should handle null/undefined values safely', async () => {
    (statsService.getStatsOverview as jest.Mock).mockResolvedValue(mockOverview);
    (statsService.getAIStats as jest.Mock).mockResolvedValue({
      ...mockAIStats,
      confidenceDistribution: {},
    });
    (statsService.getTeamStats as jest.Mock).mockResolvedValue([]);
    (statsService.getWeeklyReport as jest.Mock).mockResolvedValue({
      ...mockWeeklyReport,
      topPerformers: undefined,
    });

    render(<StatsPage />);

    await waitFor(() => {
      expect(screen.getByText('📈 통계')).toBeInTheDocument();
    });

    // Should not crash with empty arrays or undefined optional fields
    expect(screen.queryByText('최고 성과자')).not.toBeInTheDocument();
    expect(screen.queryByText('John Doe')).not.toBeInTheDocument();
  });

  it('should correctly map API response fields', async () => {
    (statsService.getStatsOverview as jest.Mock).mockResolvedValue(mockOverview);
    (statsService.getAIStats as jest.Mock).mockResolvedValue(mockAIStats);
    (statsService.getTeamStats as jest.Mock).mockResolvedValue(mockTeamStats);
    (statsService.getWeeklyReport as jest.Mock).mockResolvedValue(mockWeeklyReport);

    render(<StatsPage />);

    await waitFor(() => {
      expect(screen.getByText('📈 통계')).toBeInTheDocument();
    });

    // Verify correct field mapping for AI stats
    expect(screen.getByText('75')).toBeInTheDocument(); // totalAIAnswered (not totalAIResponses)
    expect(screen.getByText('60건')).toBeInTheDocument(); // autoResolvedCount

    // Verify correct field mapping for team stats
    expect(screen.getByText('20')).toBeInTheDocument(); // assignedCount (not assignedTasks)
    expect(screen.getByText('15')).toBeInTheDocument(); // completedCount (not completedTasks)
    expect(screen.getByText('3.5h')).toBeInTheDocument(); // avgProcessingTimeHours

    // Verify correct field mapping for weekly report
    expect(screen.getByText('85')).toBeInTheDocument(); // newInquiries (not totalInquiries)
    expect(screen.getByText('40')).toBeInTheDocument(); // newTasks (not totalTasks)
  });

  it('should calculate average confidence from distribution', async () => {
    (statsService.getStatsOverview as jest.Mock).mockResolvedValue(mockOverview);
    (statsService.getAIStats as jest.Mock).mockResolvedValue(mockAIStats);
    (statsService.getTeamStats as jest.Mock).mockResolvedValue(mockTeamStats);
    (statsService.getWeeklyReport as jest.Mock).mockResolvedValue(mockWeeklyReport);

    render(<StatsPage />);

    await waitFor(() => {
      expect(screen.getByText('📈 통계')).toBeInTheDocument();
    });

    // Average confidence should be calculated from distribution
    // HIGH: 40 * 0.9 = 36, MEDIUM: 25 * 0.5 = 12.5, LOW: 10 * 0.3 = 3
    // Total: 51.5 / 75 = 0.686... ≈ 68.7%
    const avgConfidenceElement = screen.getByText(/68\.\d%/);
    expect(avgConfidenceElement).toBeInTheDocument();
  });

  it('should handle empty confidence distribution', async () => {
    (statsService.getStatsOverview as jest.Mock).mockResolvedValue(mockOverview);
    (statsService.getAIStats as jest.Mock).mockResolvedValue({
      ...mockAIStats,
      confidenceDistribution: {},
    });
    (statsService.getTeamStats as jest.Mock).mockResolvedValue(mockTeamStats);
    (statsService.getWeeklyReport as jest.Mock).mockResolvedValue(mockWeeklyReport);

    render(<StatsPage />);

    await waitFor(() => {
      expect(screen.getByText('📈 통계')).toBeInTheDocument();
    });

    // Should show 0.0% for empty distribution
    const aiStatsSection = screen.getByText('🤖 AI 성과').closest('div');
    expect(aiStatsSection).toHaveTextContent('0.0%');
  });
});
