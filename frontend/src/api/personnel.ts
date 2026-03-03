import api from './client';
import { Person } from '../types';

export const getPersonnel = (baseId?: number) => 
  api.get<Person[]>('/personnel', { params: { baseId } }).then(res => res.data);

export const getPerson = (id: number) => 
  api.get<Person>(`/personnel/${id}`).then(res => res.data);

export const createPerson = (person: Omit<Person, 'id'>) => 
  api.post<Person>('/personnel', person).then(res => res.data);

export const updatePerson = (id: number, person: Partial<Person>) => 
  api.put<Person>(`/personnel/${id}`, person).then(res => res.data);

export const deletePerson = (id: number) => 
  api.delete(`/personnel/${id}`);
