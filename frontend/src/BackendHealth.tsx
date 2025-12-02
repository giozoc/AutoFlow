import { useEffect, useState } from 'react'

export function BackendHealth() {
    const [msg, setMsg] = useState('checking...')

    useEffect(() => {
        fetch('/api/health')
            .then((r) => r.text())
            .then((text) => setMsg(text))
            .catch((err) => {
                console.error(err)
                setMsg('errore nel contattare il backend')
            })
    }, [])

    return <h2>Backend status: {msg}</h2>
}