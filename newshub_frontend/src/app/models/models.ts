export enum UserRole {
  USER = 'USER',
  AUTHOR = 'AUTHOR',
  ADMIN = 'ADMIN'
}

export interface UserDTO {
  id: number;
  login: string;
  role: UserRole;
  isBlocked: boolean;
}

export interface AuthResponseDTO {
  token: string;
  user: UserDTO;
}

export interface CategoryDTO {
  id: number;
  name: string;
}

export interface NewsDTO {
  id: number;
  title: string;
  summary: string;
  content: string;
  author: UserDTO;
  category: CategoryDTO;
  createdAt: string;
}

export interface NewsPreviewDTO {
  id: number;
  title: string;
  summary: string;
  author: UserDTO;
  category: CategoryDTO;
  createdAt: string;
}

export interface NewsInputDTO {
  title: string;
  content: string;
  categoryId: number;
}

export interface SubscriptionsDTO {
  categories: CategoryDTO[];
  authors: UserDTO[];
}

export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}
