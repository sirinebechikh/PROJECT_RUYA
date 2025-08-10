export interface SuiviCtrBoConfig {
  localApiUrl: string;
  carthagoApiUrl: string;
  timeout: number;
  retryAttempts: number;
  refreshInterval: number;
}

export function getConfig(): SuiviCtrBoConfig {
  return {
    localApiUrl: 'http://localhost:8081/api',
    carthagoApiUrl: 'http://localhost:8082/api', // Simulation Carthago
    timeout: 10000, // 10 secondes
    retryAttempts: 3,
    refreshInterval: 30000 // 30 secondes
  };
} 