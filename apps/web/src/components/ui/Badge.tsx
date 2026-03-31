import React from 'react';

interface BadgeProps {
  children: React.ReactNode;
  variant?: 'default' | 'success' | 'warning' | 'danger' | 'info';
  size?: 'sm' | 'md';
}

const variantStyles = {
  default: 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-200',
  success: 'bg-green-100 dark:bg-green-900 text-green-700 dark:text-green-200',
  warning: 'bg-yellow-100 dark:bg-yellow-900 text-yellow-700 dark:text-yellow-200',
  danger: 'bg-red-100 dark:bg-red-900 text-red-700 dark:text-red-200',
  info: 'bg-blue-100 dark:bg-blue-900 text-blue-700 dark:text-blue-200',
};

const sizeStyles = {
  sm: 'px-2 py-0.5 text-xs',
  md: 'px-3 py-1 text-sm',
};

export function Badge({ children, variant = 'default', size = 'sm' }: BadgeProps) {
  return (
    <span
      className={`inline-flex items-center rounded-full font-medium ${variantStyles[variant]} ${sizeStyles[size]}`}
    >
      {children}
    </span>
  );
}
