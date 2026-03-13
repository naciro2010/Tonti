/**
 * AI API Service - Communication avec l'agent IA Tonti
 */

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'

// ============================================
// Types
// ============================================

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  timestamp: string
}

export interface ChatRequest {
  message: string
  daretId?: string
  conversationHistory: ChatMessage[]
}

export type ArtifactType = 'MERMAID' | 'EXCALIDRAW' | 'PDF' | 'XLSX' | 'TABLE' | 'KPI'

export interface Artifact {
  id: string
  type: ArtifactType
  title: string
  data: any
  createdAt: string
}

export interface ChatResponse {
  message: string
  artifacts: Artifact[]
  timestamp: string
}

export interface TableData {
  headers: string[]
  rows: (string | number | boolean)[][]
}

export interface KpiData {
  label: string
  value: string | number
  unit: string
  trend: 'up' | 'down' | 'stable'
}

export interface MermaidData {
  diagram: string
}

export interface PdfData {
  title?: string
  sections: { heading: string; content: string }[]
  headers?: string[]
  rows?: any[][]
  footer?: string
}

export interface XlsxData {
  headers: string[]
  rows: any[][]
  sheetName?: string
}

// ============================================
// API Functions
// ============================================

function getHeaders(): HeadersInit {
  const token = localStorage.getItem('tonti:accessToken')
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }
}

export async function sendChatMessage(request: ChatRequest): Promise<ChatResponse> {
  const response = await fetch(`${API_BASE_URL}/ai/chat`, {
    method: 'POST',
    headers: getHeaders(),
    body: JSON.stringify(request),
  })

  if (!response.ok) {
    throw new Error(`AI chat failed: ${response.statusText}`)
  }

  const data = await response.json()
  return data.data as ChatResponse
}

export async function downloadPdf(artifactData: any): Promise<Blob> {
  const response = await fetch(`${API_BASE_URL}/ai/artifacts/pdf`, {
    method: 'POST',
    headers: getHeaders(),
    body: JSON.stringify(artifactData),
  })

  if (!response.ok) {
    throw new Error(`PDF generation failed: ${response.statusText}`)
  }

  return response.blob()
}

export async function downloadXlsx(artifactData: any): Promise<Blob> {
  const response = await fetch(`${API_BASE_URL}/ai/artifacts/xlsx`, {
    method: 'POST',
    headers: getHeaders(),
    body: JSON.stringify(artifactData),
  })

  if (!response.ok) {
    throw new Error(`XLSX generation failed: ${response.statusText}`)
  }

  return response.blob()
}
