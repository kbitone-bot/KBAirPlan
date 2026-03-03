import api from './client';
import { MissionTemplate, AircraftType } from '../types';

export const getTemplates = (aircraftType?: AircraftType) => 
  api.get<MissionTemplate[]>('/templates', { params: { aircraftType } }).then(res => res.data);

export const getTemplate = (id: number) => 
  api.get<MissionTemplate>(`/templates/${id}`).then(res => res.data);

export const createTemplate = (template: Omit<MissionTemplate, 'id'>) => 
  api.post<MissionTemplate>('/templates', template).then(res => res.data);

export const updateTemplate = (id: number, template: Partial<MissionTemplate>) => 
  api.put<MissionTemplate>(`/templates/${id}`, template).then(res => res.data);

export const deleteTemplate = (id: number) => 
  api.delete(`/templates/${id}`);
