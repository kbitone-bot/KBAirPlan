import { useState } from 'react'
import { useQuery } from 'react-query'
import { Plus, Search, Plane, AlertCircle } from 'lucide-react'
import { getAircrafts, getBases } from '../api'

export default function Assets() {
  const [selectedBase, setSelectedBase] = useState<number>()
  const [searchTerm, setSearchTerm] = useState('')
  
  const { data: bases } = useQuery('bases', getBases)
  const { data: aircrafts } = useQuery(['aircrafts', selectedBase], () => 
    getAircrafts(selectedBase)
  )

  const filteredAircrafts = aircrafts?.filter(aircraft => 
    aircraft.tailNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
    aircraft.model.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const availableCount = filteredAircrafts?.filter(a => a.available).length || 0
  const unavailableCount = (filteredAircrafts?.length || 0) - availableCount

  return (
    <div className="container">
      <div className="page-header">
        <h1>기체 관리</h1>
        <button className="btn btn-primary">
          <Plus size={16} style={{ marginRight: 8 }} />
          기체 등록
        </button>
      </div>

      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
        <div className="stat-card">
          <h3>총 기체</h3>
          <div className="value">{filteredAircrafts?.length || 0}</div>
        </div>
        <div className="stat-card">
          <h3>가용 기체</h3>
          <div className="value" style={{ color: '#28a745' }}>{availableCount}</div>
        </div>
        <div className="stat-card">
          <h3>비가용 기체</h3>
          <div className="value" style={{ color: '#dc3545' }}>{unavailableCount}</div>
        </div>
      </div>

      <div className="card" style={{ marginBottom: '20px' }}>
        <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
          <div style={{ position: 'relative', flex: 1 }}>
            <Search size={20} style={{ 
              position: 'absolute', 
              left: '12px', 
              top: '50%', 
              transform: 'translateY(-50%)',
              color: '#999'
            }} />
            <input 
              type="text" 
              className="form-input"
              placeholder="기체 번호, 모델로 검색..."
              style={{ paddingLeft: '40px' }}
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <select 
            className="form-select" 
            style={{ width: '200px' }}
            value={selectedBase || ''}
            onChange={(e) => setSelectedBase(e.target.value ? Number(e.target.value) : undefined)}
          >
            <option value="">모든 기지</option>
            {bases?.map(base => (
              <option key={base.id} value={base.id}>{base.name}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="grid grid-2">
        {filteredAircrafts?.map((aircraft) => (
          <div key={aircraft.id} className="card" style={{ 
            display: 'flex', 
            alignItems: 'center',
            gap: '16px',
            borderLeft: `4px solid ${aircraft.available ? '#28a745' : '#dc3545'}`
          }}>
            <div style={{ 
              width: '60px', 
              height: '60px', 
              backgroundColor: '#f0f0f0',
              borderRadius: '8px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
              <Plane size={32} color="#666" />
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: '18px', fontWeight: '600', marginBottom: '4px' }}>
                {aircraft.tailNumber}
              </div>
              <div style={{ color: '#666', fontSize: '14px' }}>
                {aircraft.model} • {aircraft.baseName}
              </div>
              <div style={{ marginTop: '8px', display: 'flex', gap: '8px' }}>
                <span className={`badge badge-${aircraft.available ? 'success' : 'danger'}`}>
                  {aircraft.available ? '가용' : '비가용'}
                </span>
                <span className="badge badge-info">
                  {aircraft.type === 'FIXED' ? '고정익' : '회전익'}
                </span>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
