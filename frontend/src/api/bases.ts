import api from './client';
import { Base } from '../types';

export const getBases = () => api.get<Base[]>('/bases').then(res => res.data);
export const getBase = (id: number) => api.get<Base>(`/bases/${id}`).then(res => res.data);
export const createBase = (base: Omit<Base, 'id'>) => api.post<Base>('/bases', base).then(res => res.data);
