import api from './client';
import { WeightConfig } from '../types';

export const getWeightConfigs = () => 
  api.get<WeightConfig[]>('/config/weights').then(res => res.data);

export const getWeightConfig = (id: number) => 
  api.get<WeightConfig>(`/config/weights/${id}`).then(res => res.data);

export const getDefaultWeightConfig = () => 
  api.get<WeightConfig>('/config/weights/default').then(res => res.data);

export const createWeightConfig = (config: Omit<WeightConfig, 'id'>) => 
  api.post<WeightConfig>('/config/weights', config).then(res => res.data);

export const updateWeightConfig = (id: number, config: Partial<WeightConfig>) => 
  api.put<WeightConfig>(`/config/weights/${id}`, config).then(res => res.data);

export const deleteWeightConfig = (id: number) => 
  api.delete(`/config/weights/${id}`);
