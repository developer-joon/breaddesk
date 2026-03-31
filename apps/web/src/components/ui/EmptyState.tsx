import React from 'react';

interface EmptyStateProps {
  icon?: string;
  title: string;
  description?: string;
  action?: {
    label: string;
    onClick: () => void;
  };
}

export function EmptyState({ icon = '📭', title, description, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 gap-3 text-center">
      <div className="text-5xl">{icon}</div>
      <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100">{title}</h3>
      {description && <p className="text-sm text-gray-500 dark:text-gray-400 max-w-sm">{description}</p>}
      {action && (
        <button
          onClick={action.onClick}
          className="mt-2 px-4 py-2 bg-blue-600 dark:bg-blue-500 text-white rounded-lg hover:bg-blue-700 dark:hover:bg-blue-600 transition-colors"
        >
          {action.label}
        </button>
      )}
    </div>
  );
}
