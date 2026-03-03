import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery, useMutation } from 'react-query'
import { format } from 'date-fns'
import { 
  Save, 
  RefreshCw, 
  AlertTriangle, 
  CheckCircle, 
  XCircle,
  Info,
  ChevronDown,
  ChevronUp
} from 'lucide-react'
import { getDraft, getWeightConfigs, recomputeDraft, updateItem } from '../api'
import { FlightPlanItem, WeightConfig } from '../types'

export default function DraftEditor() {
  const { id } = useParams<{ id: string }>()
  const draftId = Number(id)
  
  const [selectedItem, setSelectedItem] = useState<FlightPlanItem | null>(null)
  const [showWeights, setShowWeights] = useState(false)

  const { data: draft, refetch } = useQuery(['draft', draftId], () => getDraft(draftId))
  const { data: weightConfigs } = useQuery('weightConfigs', getWeightConfigs)

  const recomputeMutation = useMutation(
    ({ draftId, weightConfigId }: { draftId: number; weightConfigId: number }) => 
      recomputeDraft(draftId, weightConfigId),
    { onSuccess: () => refetch() }
  )

  const updateItemMutation = useMutation(
    ({ itemId, data }: { itemId: number; data: { assignedPersonIds: number[] } }) =>
      updateItem(draftId, itemId, data),
    { onSuccess: () => refetch() }
  )

  const handleWeightChange = (configId: number) => {
    recomputeMutation.mutate({ draftId, weightConfigId: configId })
  }

  return (
    <div className="container">
      <div className="page-header">
        <div>
          <h1>비행계획 편집</h1>
          <div style={{ color: '#666', fontSize: '14px', marginTop: '4px' }}>
            {draft?.baseName} • {draft?.startDate} ~ {draft?.endDate}
          </div>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button 
            className="btn btn-secondary"
            onClick={() => setShowWeights(!showWeights)}
          >
            <RefreshCw size={16} style={{ marginRight: 8 }} />
            재계산
          </button>
          <button className="btn btn-primary">
            <Save size={16} style={{ marginRight: 8 }} />
            저장
          </button>
        </div>
      </div>

      {showWeights && (
        <div className="card" style={{ marginBottom: '20px', backgroundColor: '#f8f9fa' }}>
          <h3 className="card-title">가중치 설정 변경</h3>
          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
            {weightConfigs?.map(config => (
              <button
                key={config.id}
                className={`btn ${draft?.weightConfigId === config.id ? 'btn-primary' : 'btn-secondary'}`}
                onClick={() => handleWeightChange(config.id)}
                disabled={recomputeMutation.isLoading}
              >
                {config.name}
              </button>
            ))}
          </div>
        </div>
      )}

      <div className="grid" style={{ gridTemplateColumns: '2fr 1fr', gap: '20px' }}>
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '16px' }}>
            <h2 className="card-title">
              비행 일정 ({draft?.items?.length || 0}건)
            </h2>
            <div style={{ display: 'flex', gap: '8px' }}>
              <span className="badge badge-success">
                <CheckCircle size={12} style={{ marginRight: 4 }} />
                배정완료: {draft?.scoreSummary?.assignedFlights || 0}
              </span>
              {draft?.scoreSummary?.hardViolations ? (
                <span className="badge badge-danger">
                  <AlertTriangle size={12} style={{ marginRight: 4 }} />
                  위반: {draft.scoreSummary.hardViolations}
                </span>
              ) : null}
            </div>
          </div>

          <div style={{ maxHeight: '600px', overflow: 'auto' }}>
            <table className="table" style={{ fontSize: '13px' }}>
              <thead>
                <tr>
                  <th>날짜</th>
                  <th>시간</th>
                  <th>기체</th>
                  <th>임무</th>
                  <th>배정 인원</th>
                  <th>스코어</th>
                  <th>상태</th>
                </tr>
              </thead>
              <tbody>
                {draft?.items?.map((item) => (
                  <tr 
                    key={item.id} 
                    onClick={() => setSelectedItem(item)}
                    style={{ 
                      cursor: 'pointer',
                      backgroundColor: selectedItem?.id === item.id ? '#e3f2fd' : 'transparent'
                    }}
                  >
                    <td>{item.flightDate}</td>
                    <td>{item.startTime}~{item.endTime}</td>
                    <td>{item.tailNumber}</td>
                    <td>{item.missionName}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '4px', flexWrap: 'wrap' }}>
                        {item.assignedPersons.slice(0, 3).map((p, idx) => (
                          <span key={idx} className="badge badge-info" style={{ fontSize: '11px' }}>
                            {p.name}
                          </span>
                        ))}
                        {item.assignedPersons.length > 3 && (
                          <span className="badge" style={{ fontSize: '11px' }}>
                            +{item.assignedPersons.length - 3}
                          </span>
                        )}
                      </div>
                    </td>
                    <td>
                      <span style={{ 
                        color: (item.score || 0) > 0 ? '#28a745' : '#dc3545',
                        fontWeight: 500
                      }}>
                        {item.score?.toFixed(1) || '-'}
                      </span>
                    </td>
                    <td>
                      {item.violations?.length ? (
                        <span className="badge badge-danger">
                          <XCircle size={12} style={{ marginRight: 4 }} />
                          위반
                        </span>
                      ) : (
                        <span className="badge badge-success">정상</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div>
          <div className="card" style={{ marginBottom: '20px' }}>
            <h2 className="card-title">스코어 요약</h2>
            <div style={{ display: 'grid', gap: '12px' }}>
              <div style={{ 
                padding: '16px', 
                backgroundColor: '#f8f9fa', 
                borderRadius: '8px',
                textAlign: 'center'
              }}>
                <div style={{ fontSize: '32px', fontWeight: '600', color: '#0066cc' }}>
                  {draft?.totalScore?.toFixed(1) || '-'}
                </div>
                <div style={{ color: '#666', fontSize: '14px' }}>총점</div>
              </div>
              
              <div style={{ display: 'grid', gap: '8px' }}>
                <ScoreBar 
                  label="공평성" 
                  value={draft?.scoreSummary?.fairnessScore || 0} 
                  color="#28a745"
                />
                <ScoreBar 
                  label="숙련도" 
                  value={draft?.scoreSummary?.skillScore || 0} 
                  color="#0066cc"
                />
                <ScoreBar 
                  label="피로도" 
                  value={draft?.scoreSummary?.fatigueScore || 0} 
                  color="#ffc107"
                />
              </div>
            </div>
          </div>

          {selectedItem && (
            <div className="card">
              <h2 className="card-title">
                <Info size={18} style={{ marginRight: 8, verticalAlign: 'middle' }} />
                배정 상세
              </h2>
              
              <div style={{ marginBottom: '16px' }}>
                <div style={{ fontWeight: 600, marginBottom: '8px' }}>
                  {selectedItem.flightDate} {selectedItem.startTime}~{selectedItem.endTime}
                </div>
                <div style={{ color: '#666', fontSize: '14px' }}>
                  {selectedItem.aircraftModel} ({selectedItem.tailNumber})
                </div>
                <div style={{ color: '#666', fontSize: '14px' }}>
                  {selectedItem.missionName}
                </div>
              </div>

              <div style={{ marginBottom: '16px' }}>
                <div style={{ fontWeight: 600, marginBottom: '8px' }}>배정된 인원</div>
                {selectedItem.assignedPersons.map((person, idx) => (
                  <div 
                    key={idx}
                    style={{
                      padding: '8px',
                      backgroundColor: '#f8f9fa',
                      borderRadius: '4px',
                      marginBottom: '4px',
                      display: 'flex',
                      justifyContent: 'space-between'
                    }}
                  >
                    <span>{person.name} ({person.rank})</span>
                    <span className={`badge badge-${person.role === 'PILOT' ? 'info' : 'success'}`}>
                      {person.role === 'PILOT' ? '조종사' : '승무원'}
                    </span>
                  </div>
                ))}
              </div>

              {selectedItem.explanation && (
                <div>
                  <div style={{ fontWeight: 600, marginBottom: '8px' }}>배정 사유</div>
                  <div style={{ fontSize: '13px', color: '#666', lineHeight: '1.6' }}>
                    {selectedItem.explanation.reason}
                  </div>
                  <div style={{ marginTop: '12px', fontSize: '12px' }}>
                    <div>숙련도 기여: +{selectedItem.explanation.skillContribution.toFixed(1)}</div>
                    <div>공평성 기여: +{selectedItem.explanation.fairnessContribution.toFixed(1)}</div>
                    <div>피로도 페널티: {selectedItem.explanation.fatiguePenalty.toFixed(1)}</div>
                    <div>연속성 페널티: {selectedItem.explanation.continuityPenalty.toFixed(1)}</div>
                    <div style={{ marginTop: '8px', fontWeight: 600 }}>
                      총점: {selectedItem.explanation.totalScore.toFixed(1)}
                    </div>
                  </div>
                </div>
              )}

              {selectedItem.violations?.length > 0 && (
                <div style={{ marginTop: '16px' }}>
                  <div style={{ fontWeight: 600, marginBottom: '8px', color: '#dc3545' }}>
                    위반 사항
                  </div>
                  {selectedItem.violations.map((violation, idx) => (
                    <div key={idx} className="badge badge-danger" style={{ marginRight: '4px' }}>
                      {violation}
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

function ScoreBar({ label, value, color }: { label: string; value: number; color: string }) {
  const percentage = Math.min(Math.max(value * 10, 0), 100)
  
  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px', fontSize: '13px' }}>
        <span>{label}</span>
        <span>{value.toFixed(1)}</span>
      </div>
      <div style={{ 
        width: '100%', 
        height: '8px', 
        backgroundColor: '#e0e0e0',
        borderRadius: '4px',
        overflow: 'hidden'
      }}>
        <div style={{ 
          width: `${percentage}%`,
          height: '100%',
          backgroundColor: color,
          borderRadius: '4px',
          transition: 'width 0.3s ease'
        }} />
      </div>
    </div>
  )
}
