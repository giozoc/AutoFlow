import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import type { VeicoloDTO } from '../../entities/vehicle'
import { getShowroomDettaglio } from '../../services/showroomService'

const ShowroomDettaglioPage: React.FC = () => {
    const { id } = useParams<{ id: string }>()
    const [veicolo, setVeicolo] = useState<VeicoloDTO | null>(null)
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        if (!id) return
        const veicoloId = Number(id)
        if (Number.isNaN(veicoloId)) return

        setLoading(true)
        setError(null)
        ;(async () => {
            try {
                const data = await getShowroomDettaglio(veicoloId)
                setVeicolo(data)
            } catch (e) {
                console.error(e)
                setError('Errore nel caricamento del veicolo.')
            } finally {
                setLoading(false)
            }
        })()
    }, [id])

    return (
        <div style={{ padding: '2rem' }}>
            <p>
                <Link to="/showroom">« Torna allo showroom</Link>
            </p>

            {error && <p style={{ color: 'red' }}>{error}</p>}
            {loading && <p>Caricamento...</p>}

            {veicolo && (
                <div
                    style={{
                        marginTop: '1rem',
                        padding: '1.5rem',
                        border: '1px solid #ddd',
                        borderRadius: '0.75rem',
                        maxWidth: '800px',
                    }}
                >
                    <h1>
                        {veicolo.marca} {veicolo.modello} ({veicolo.anno})
                    </h1>
                    <p>Prezzo base: {veicolo.prezzoBase.toLocaleString('it-IT')} €</p>
                    <p>Chilometraggio: {veicolo.chilometraggio.toLocaleString('it-IT')} km</p>
                    <p>Alimentazione: {veicolo.alimentazione}</p>
                    <p>Cambio: {veicolo.cambio}</p>
                    <p>Colore esterno: {veicolo.coloreEsterno}</p>

                    <p style={{ marginTop: '1rem' }}>
                        Targa: <strong>{veicolo.targa}</strong>
                    </p>
                    <p>VIN: {veicolo.vin}</p>

                    <p style={{ marginTop: '1rem', fontStyle: 'italic' }}>
                        Stato: {veicolo.stato}
                    </p>
                </div>
            )}
        </div>
    )
}

export default ShowroomDettaglioPage