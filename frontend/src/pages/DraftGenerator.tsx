import { useState } from 'react'
import { useQuery, useMutation } from 'react-query'
import { useNavigate } from 'react-router-dom'
import { format, addDays } from 'date-fns'
import { Calendar, Settings, Plane, MapPin, ChevronRight } from 'lucide-react'
import { getBases, getTemplates, getWeightConfigs, generateDraft } from '../api'
import { GenerateDraftRequest, PeriodType, AircraftType } from '../types'

export default function DraftGenerator() {
  const navigate = useNavigate()
  
  const [formData, setFormData] = useState<Partial<GenerateDraftRequest>>({
    periodType: 'WEEK' as PeriodType,
    startDate: format(new Date(), 'yyyy-MM-dd'),
    endDate: format(addDays(new Date(), 7), 'yyyy-MM-dd'),
    aircraftType: 'FIXED' as AircraftType,
    flightsPerDay: 3,
  })

  const { data: bases } = useQuery('bases', getBases)
  const { data: templates } = useQuery('templates', () => getTemplates())
  const { data: weightConfigs } = useQuery('weightConfigs', getWeightConfigs)

  const generateMutation = useMutation(generateDraft, {
    onSuccess: (data) => {
      navigate(`/drafts/${data.id}`)
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (formData.baseId && formData.weightConfigId) {
      generateMutation.mutate(formData as GenerateDraftRequest)
    }
  }

  return (
    <div className="container">
      <div className="page-header">
        <h1>비행계획 생성</h1>
      </div>

      <div className="grid grid-2">
        <div className="card">
          <h2 className="card-title">계획 설정</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">기지 선택</label>
              <select 
                className="form-select"
                value={formData.baseId || ''}
                onChange={(e) => setFormData({ ...formData, baseId: Number(e.target.value) })}
              >
                <option value="">기지를 선택하세요</option>
                {bases?.map(base => (
                  <option key={base.id} value={base.id}>{base.name}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">기간 유형</label>
              <select 
                className="form-select"
                value={formData.periodType}
                onChange={(e) => {
                  const type = e.target.value as PeriodType
                  let endDate = formData.startDate || format(new Date(), 'yyyy-MM-dd')
                  if (type === 'DAY') {
                    endDate = formData.startDate || format(new Date(), 'yyyy-MM-dd')
                  } else if (type === 'WEEK') {
                    endDate = format(addDays(new Date(formData.startDate || new Date()), 7), 'yyyy-MM-dd')
                  } else {
                    endDate = format(addDays(new Date(formData.startDate || new Date()), 30), 'yyyy-MM-dd')
                  }
                  setFormData({ ...formData, periodType: type, endDate })
                }}
              >
                <option value="DAY">일간</option>
                <option value="WEEK">주간</option>
                <option value="MONTH">월간</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">기간</label>
              <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                <input 
                  type="date" 
                  className="form-input"
                  value={formData.startDate}
                  onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                />
                <span>~</span>
                <input 
                  type="date" 
                  className="form-input"
                  value={formData.endDate}
                  onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">기종</label>
              <select 
                className="form-select"
                value={formData.aircraftType}
                onChange={(e) => setFormData({ ...formData, aircraftType: e.target.value as AircraftType })}
              >
                <option value="FIXED">고정익</option>
                <option value="ROTARY">회전익</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">일일 비행 횟수</label>
              <input 
                type="number" 
                className="form-input"
                min={1}
                max={10}
                value={formData.flightsPerDay}
                onChange={(e) => setFormData({ ...formData, flightsPerDay: Number(e.target.value) })}
              />
            </div>

            <div className="form-group">
              <label className="form-label">가중치 설정</label>
              <select 
                className="form-select"
                value={formData.weightConfigId || ''}
                onChange={(e) => setFormData({ ...formData, weightConfigId: Number(e.target.value) })}
              >
                <option value="">설정을 선택하세요</option>
                {weightConfigs?.map(config => (
                  <option key={config.id} value={config.id}>{config.name}</option>
                ))}
              </select>
            </div>

            <button 
              type="submit" 
              className="btn btn-primary"
              style={{ width: '100%', marginTop: '16px' }}
              disabled={generateMutation.isLoading}
            >
              {generateMutation.isLoading ? '생성 중...' : '계획 생성'}
            </button>
          </form>
        </div>

        <div>
          <div className="card" style={{ marginBottom: '20px' }}>
            <h2 className="card-title">
              <Settings size={20} style={{ marginRight: 8, verticalAlign: 'middle' }} />
              가중치 설명
            </h2>
            <div style={{ color: '#666', lineHeight: '1.6' }}>
              <p style={{ marginBottom: '12px' }}>
                <strong>공평성:</strong> 업무 분산 정도를 고려합니다. 높을수록 모든 인원에게 고르게 배정됩니다.
              </p>
              <p style={{ marginBottom: '12px' }}>
                <strong>숙련도:</strong> 경험이 많은 인원을 우선 배정합니다. 높을수록 숙련자를 선호합니다.
              </p>
              <p style={{ marginBottom: '12px' }}>
                <strong>피로도:</strong> 연속/야간/주말 배정에 페널티를 부여합니다. 높을수록 피로를 회피합니다.
              </p>
              <p>
                <strong>연속성:</strong> 연속 배정을 억제합니다. 높을수록 휴식을 보장합니다.
              </p>
            </div>
          </div>

          <div className="card">
            <h2 className="card-title">
              <Plane size={20} style={{ marginRight: 8, verticalAlign: 'middle' }} />
              사용 가능한 임무
            </h2>
            <div style={{ maxHeight: '300px', overflow: 'auto' }}>
              {templates?.filter(t => t.aircraftType === formData.aircraftType).map(template => (
                <div 
                  key={template.id} 
                  style={{ 
                    padding: '12px', 
                    borderBottom: '1px solid #eee',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                  }}
                >
                  <span>{template.missionName}</span>
                  <span style={{ color: '#666', fontSize: '12px' }}>
                    {template.requiredPilotCount}조/{template.requiredCrewCount}승
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
