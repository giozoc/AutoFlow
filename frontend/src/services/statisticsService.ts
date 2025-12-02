// src/services/statisticsService.ts

import axios from 'axios'
import type { DashboardStatisticsDTO } from '../entities/statistics'
import { authHeaders } from './authService'

const API_BASE_URL = 'http://localhost:8080/api/statistics'

export async function getDashboardStatistics(): Promise<DashboardStatisticsDTO> {
    const res = await axios.get<DashboardStatisticsDTO>(
        `${API_BASE_URL}/dashboard`,
        {
            headers: authHeaders(),
        },
    )
    return res.data
}