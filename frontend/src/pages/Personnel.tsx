import { useState } from 'react'
import { useQuery } from 'react-query'
import { Plus, Search, Filter } from 'lucide-react'
import { getPersonnel, getBases } from '../api'
import { Person } from '../types'

export default function Personnel() {
  const [selectedBase, setSelectedBase] = useState<number>()
  const [searchTerm, setSearchTerm] = useState('')
  
  const { data: bases } = useQuery('bases', getBases)
  const { data: personnel } = useQuery(['personnel', selectedBase], () => 
    getPersonnel(selectedBase)
  )

  const filteredPersonnel = personnel?.filter(person => 
    person.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    person.rank.toLowerCase().includes(searchTerm.toLowerCase())
  )

  return (
    <div className="container">
      <div className="page-header">
        <h1>인원 관리</h1>
        <button className="btn btn-primary">
          <Plus size={16} style={{ marginRight: 8 }} />
          인원 등록
        </button>
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
              placeholder="이름, 계급으로 검색..."
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

      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>이름</th>
              <th>역할</th>
              <th>계급</th>
              <th>소속 기지</th>
              <th>총 비행 시간</th>
              <th>월 비행 횟수</th>
              <th>상태</th>
            </tr>
          </thead>
          <tbody>
            {filteredPersonnel?.map((person) => (
              <tr key={person.id}>
                <td>{person.id}</td>
                <td>{person.name}</td>
                <td>
                  <span className={`badge badge-${person.role === 'PILOT' ? 'info' : 'success'}`}>
                    {person.role === 'PILOT' ? '조종사' : '승무원'}
                  </span>
                </td>
                <td>{person.rank}</td>
                <td>{person.baseName}</td>
                <td>{person.totalFlightHours}시간</td>
                <td>{person.monthlyFlightCount}회</td>
                <td>
                  <span className={`badge badge-${person.status === 'ACTIVE' ? 'success' : 'danger'}`}>
                    {person.status === 'ACTIVE' ? '활동중' : '비활동'}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {filteredPersonnel?.length === 0 && (
          <div style={{ textAlign: 'center', padding: '40px', color: '#999' }}>
            검색 결과가 없습니다.
          </div>
        )}
      </div>
    </div>
  )
}
