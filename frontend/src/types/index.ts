export interface Base {
  id: number;
  name: string;
  location: string;
}

export type Role = 'PILOT' | 'CREW';
export type Status = 'ACTIVE' | 'INACTIVE';

export interface Person {
  id: number;
  baseId: number;
  baseName: string;
  role: Role;
  name: string;
  rank: string;
  status: Status;
  totalFlightHours: number;
  monthlyFlightCount: number;
  qualifications: Qualification[];
  createdAt: string;
}

export interface Qualification {
  id: number;
  name: string;
  type: string;
  validFrom: string;
  validTo: string;
}

export type AircraftType = 'FIXED' | 'ROTARY';

export interface Aircraft {
  id: number;
  type: AircraftType;
  model: string;
  tailNumber: string;
  baseId: number;
  baseName: string;
  available: boolean;
  maxCrew: number;
  createdAt: string;
}

export interface MissionTemplate {
  id: number;
  aircraftType: AircraftType;
  missionName: string;
  requiredPilotCount: number;
  requiredCrewCount: number;
  durationMinutes: number;
  description: string;
  requiredQualificationIds: number[];
  createdAt: string;
}

export type PeriodType = 'DAY' | 'WEEK' | 'MONTH';

export interface WeightConfig {
  id: number;
  name: string;
  fairnessWeight: number;
  skillWeight: number;
  fatigueWeight: number;
  continuityWeight: number;
  baseBalanceWeight: number;
  isDefault: boolean;
  createdAt: string;
}

export interface FlightPlanItem {
  id: number;
  flightDate: string;
  startTime: string;
  endTime: string;
  aircraftId: number;
  aircraftModel: string;
  tailNumber: string;
  missionTemplateId: number;
  missionName: string;
  airspaceId?: number;
  airspaceName?: string;
  assignedPersons: AssignedPerson[];
  notes?: string;
  score: number;
  status: string;
  violations: string[];
  explanation: AssignmentExplanation;
}

export interface AssignedPerson {
  personId: number;
  name: string;
  rank: string;
  role: string;
}

export interface AssignmentExplanation {
  skillContribution: number;
  fairnessContribution: number;
  fatiguePenalty: number;
  continuityPenalty: number;
  totalScore: number;
  reason: string;
}

export interface DraftResponse {
  id: number;
  baseId: number;
  baseName: string;
  periodType: PeriodType;
  startDate: string;
  endDate: string;
  weightConfigId: number;
  weightConfigName: string;
  status: string;
  totalScore: number;
  items: FlightPlanItem[];
  scoreSummary: ScoreSummary;
  violations: Violation[];
  createdAt: string;
}

export interface ScoreSummary {
  totalFlights: number;
  assignedFlights: number;
  hardViolations: number;
  softViolations: number;
  averageScore: number;
  fairnessScore: number;
  skillScore: number;
  fatigueScore: number;
}

export interface Violation {
  itemId: number;
  type: string;
  severity: string;
  message: string;
}

export interface GenerateDraftRequest {
  baseId: number;
  periodType: PeriodType;
  startDate: string;
  endDate: string;
  aircraftType: AircraftType;
  missionTemplateIds: number[];
  weightConfigId: number;
  flightsPerDay?: number;
}

export interface MonthlySummary {
  month: string;
  baseId: number;
  baseName: string;
  summary: {
    totalFlights: number;
    totalPersonnel: number;
    totalAircraft: number;
    avgPersonsPerFlight: number;
    missionsByType: Record<string, number>;
  };
  personStats: PersonStat[];
  aircraftStats: AircraftStat[];
  dailyStats: DailyStat[];
}

export interface PersonStat {
  personId: number;
  name: string;
  role: string;
  flightCount: number;
  flightHours: number;
}

export interface AircraftStat {
  aircraftId: number;
  model: string;
  tailNumber: string;
  flightCount: number;
  flightHours: number;
}

export interface DailyStat {
  date: string;
  flightCount: number;
  personnelCount: number;
}
