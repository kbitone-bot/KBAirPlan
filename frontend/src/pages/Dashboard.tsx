import { useEffect, useState } from 'react'
import { useQuery } from 'react-query'
import { format } from 'date-fns'
import { ko } from 'date-fns/locale'
import { 
  Users, 
  Plane, 
  Calendar, 
  AlertCircle,
  TrendingUp,
  TrendingDown
} from 'lucide-react'
import { getBases, getPersonnel, getAircrafts, getMonthlySummary } from '../api'

export default function Dashboard() {
  const [selectedMonth, setSelectedMonth] = useState(format(new Date(), 'yyyy-MM'))
  const [selectedBase, setSelectedBase] = useState<number>(1)
  
  const { data: bases } = useQuery('bases', getBases)
  const { data: personnel } = useQuery('personnel', () => getPersonnel())
  const { data: aircrafts } = useQuery('aircrafts', () => getAircrafts())
  const { data: summary } = useQuery(
    ['summary', selectedBase, selectedMonth], 
    () => getMonthlySummary(selectedBase, selectedMonth),
    { enabled: !!selectedBase }
  )

  const stats = [
    { 
      title: '총 인원', 
      value: personnel?.length || 0, 
      icon: Users,
      change: '+2',
      trend: 'up'
    },
    { 
      title: '보유 기체', 
      value: aircrafts?.length || 0, 
      icon: Plane,
      change: '0',
      trend: 'neutral'
    },
    { 
      title: '이번 달 비행', 
      value: summary?.summary.totalFlights || 0, 
      icon: Calendar,
      change: '+15',
      trend: 'up'
    },
    { 
      title: '위반 사항', 
      value: summary?.summary.totalFlights ? Math.floor(summary.summary.totalFlights * 0.05) : 0, 
      icon: AlertCircle,
      change: '-3',
      trend: 'down'
    },
  ]

  return (
    <div className="container">
      <div className="page-header">
        <h1>대시보드</h1>
        <div style={{ display: 'flex', gap: '12px' }}>
          <select 
            className="form-select" 
            style={{ width: 'auto' }}
            value={selectedBase}
            onChange={(e) => setSelectedBase(Number(e.target.value))}
          >
            {bases?.map(base => (
              <option key={base.id} value={base.id}>{base.name} 기지</option>
            ))}
          </select>
          <input 
            type="month" 
            className="form-input"
            style={{ width: 'auto' }}
            value={selectedMonth}
            onChange={(e) => setSelectedMonth(e.target.value)}
          />
        </div>
      </div>

      <div className="stats-grid">
        {stats.map((stat, index) => (
          <div key={index} className="stat-card">
            <h3>{stat.title}</h3>
            <div className="value">{stat.value}</div>
            <div className="change" style={{ 
              color: stat.trend === 'up' ? '#28a745' : stat.trend === 'down' ? '#dc3545' : '#666'
            }}>
              {stat.trend === 'up' && <TrendingUp size={14} style={{ marginRight: 4 }} />}
              {stat.trend === 'down' && <TrendingDown size={14} style={{ marginRight: 4 }} />}
              {stat.change} 지난 달 대비
            </div>
          </div>
        ))}
      </div>

      <div className="content-grid">
        <div className="card">
          <h2 className="card-title">임물별 통계</h2>
          {summary?.summary.missionsByType && (
            <table className="table">
              <thead>
                <tr>
                  <th>임무 유형</th>
                  <th>횟수</th>
                  <th>비율</th>
                </tr>
              </thead>
              <tbody>
                {Object.entries(summary.summary.missionsByType).map(([mission, count]) => (
                  <tr key={mission}>
                    <td>{mission}</td>
                    <td>{count}</td>
                    <td>
                      <div style={{ 
                        width: `${(count / summary.summary.totalFlights) * 100}%`,
                        backgroundColor: '#0066cc',
                        height: '20px',
                        borderRadius: '4px',
                        minWidth: '20px'
                      }} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="card">
          <h2 className="card-title">인원별 비행 통계 (Top 10)</h2>
          {summary?.personStats && (
            <table className="table">
              <thead>
                <tr>
                  <th>이름</th>
                  <th>역할</th>
                  <th>횟수</th>
                </tr>
              </thead>
              <tbody>
                {summary.personStats.slice(0, 10).map((person) => (
                  <tr key={person.personId}>
                    <td>{person.name}</td>
                    <td>
                      <span className={`badge badge-${person.role === 'PILOT' ? 'info' : 'success'}`}>
                        {person.role === 'PILOT' ? '조종사' : '승무원'}
                      </span>
                    </td>
                    <td>{person.flightCount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      <div className="card">
        <h2 className="card-title">일별 비행 현황</h2>
        <div style={{ display: 'flex', gap: '4px', flexWrap: 'wrap' }}>
          {summary?.dailyStats.map((day) => (
            <div 
              key={day.date}
              style={{
                width: '32px',
                height: '32px',
                backgroundColor: day.flightCount > 0 
                  ? `rgba(0, 102, 204, ${Math.min(day.flightCount / 10, 1)})` 
                  : '#e0e0e0',
                borderRadius: '4px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '11px',
                color: day.flightCount > 5 ? 'white' : '#333'
              }}
              title={`${day.date}: ${day.flightCount}회`}
            >
              {new Date(day.date).getDate()}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
