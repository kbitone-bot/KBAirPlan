import api from './client';
import { 
  DraftResponse, 
  GenerateDraftRequest, 
  UpdateItemRequest, 
  FlightPlanItemResponse,
  MonthlySummary 
} from '../types';

export const generateDraft = (request: GenerateDraftRequest) => 
  api.post<DraftResponse>('/plans/draft/generate', request).then(res => res.data);

export const getDraft = (draftId: number) => 
  api.get<DraftResponse>(`/plans/draft/${draftId}`).then(res => res.data);

export const updateItem = (draftId: number, itemId: number, request: UpdateItemRequest) => 
  api.patch<FlightPlanItemResponse>(`/plans/draft/${draftId}/items/${itemId}`, request).then(res => res.data);

export const recomputeDraft = (draftId: number, weightConfigId: number) => 
  api.post<DraftResponse>(`/plans/draft/${draftId}/recompute`, null, { 
    params: { weightConfigId } 
  }).then(res => res.data);

export const getMonthlySummary = (baseId: number, month: string) => 
  api.get<MonthlySummary>('/plans/summary/month', { 
    params: { baseId, month } 
  }).then(res => res.data);
