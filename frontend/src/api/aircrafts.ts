import api from './client';
import { Aircraft, AircraftType } from '../types';

export const getAircrafts = (baseId?: number, type?: AircraftType) => 
  api.get<Aircraft[]>('/aircrafts', { params: { baseId, type } }).then(res => res.data);

export const getAircraft = (id: number) => 
  api.get<Aircraft>(`/aircrafts/${id}`).then(res => res.data);

export const createAircraft = (aircraft: Omit<Aircraft, 'id'>) => 
  api.post<Aircraft>('/aircrafts', aircraft).then(res => res.data);

export const updateAircraft = (id: number, aircraft: Partial<Aircraft>) => 
  api.put<Aircraft>(`/aircrafts/${id}`, aircraft).then(res => res.data);

export const deleteAircraft = (id: number) => 
  api.delete(`/aircrafts/${id}`);
