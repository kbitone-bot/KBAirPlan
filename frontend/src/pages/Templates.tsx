import { useState } from 'react'
import { useQuery } from 'react-query'
import { Plus, Edit2, Trash2, Clock, Users } from 'lucide-react'
import { getTemplates } from '../api'

export default function Templates() {
  const [selectedType, setSelectedType] = useState<string>('')
  
  const { data: templates } = useQuery('templates', () => getTemplates())

  const filteredTemplates = templates?.filter(template => 
    !selectedType || template.aircraftType === selectedType
  )

  return (
    <div className="container">
      <div className="page-header">
        <h1>임무 템플릿</h1>
        <button className="btn btn-primary">
          <Plus size={16} style={{ marginRight: 8 }} />
          템플릿 생성
        </button>
      </div>

      <div className="card" style={{ marginBottom: '20px' }}>
        <div style={{ display: 'flex', gap: '16px' }}>
          <select 
            className="form-select" 
            style={{ width: '200px' }}
            value={selectedType}
            onChange={(e) => setSelectedType(e.target.value)}
          >
            <option value="">모든 기종</option>
            <option value="FIXED">고정익</option>
            <option value="ROTARY">회전익</option>
          </select>
        </div>
      </div>

      <div className="grid grid-3">
        {filteredTemplates?.map((template) => (
          <div key={template.id} className="card">
            <div style={{ 
              display: 'flex', 
              justifyContent: 'space-between', 
              alignItems: 'flex-start',
              marginBottom: '12px'
            }}>
              <h3 style={{ fontSize: '18px', fontWeight: '600' }}>{template.missionName}</h3>
              <span className={`badge badge-${template.aircraftType === 'FIXED' ? 'info' : 'success'}`}>
                {template.aircraftType === 'FIXED' ? '고정익' : '회전익'}
              </span>
            </div>
            
            <p style={{ color: '#666', fontSize: '14px', marginBottom: '16px' }}>
              {template.description}
            </p>
            
            <div style={{ display: 'flex', gap: '16px', marginBottom: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#666' }}>
                <Users size={16} />
                <span>조종사 {template.requiredPilotCount}명</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#666' }}>
                <Users size={16} />
                <span>승무원 {template.requiredCrewCount}명</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#666' }}>
                <Clock size={16} />
                <span>{template.durationMinutes}분</span>
              </div>
            </div>
            
            <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
              <button className="btn btn-secondary" style={{ padding: '6px 12px' }}>
                <Edit2 size={14} />
              </button>
              <button className="btn btn-danger" style={{ padding: '6px 12px' }}>
                <Trash2 size={14} />
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
