import React, {useState} from "react";
import {useLocation, useParams} from "react-router-dom";
import {
    Alert,
    BodyLong,
    BodyShort,
    Button,
    Heading,
    Label,
    Loader,
    Radio,
    RadioGroup,
    ReadMore
} from "@navikt/ds-react";
import "@navikt/ds-css";
import {fetch} from "whatwg-fetch";
import './Journalpost.css';


const STATUS_INITIAL = 'INITIAL';
const STATUS_READY_FOR_INPUT = 'READY_FOR_INPUT';
const STATUS_SUBMITTING = 'SUBMITTING';
const STATUS_SUBMITTED = 'SUBMITTED';

async function distribuerJournalpost({id, status, distribusjonstype}, setStatus, setErrorMessage) {
    console.log('distribuer', id);
    console.log('status', status);
    setStatus(STATUS_SUBMITTING)
    const response = await fetch(`/api/journalpost/${id}/send`, {
        method: 'POST',
        credentials: 'same-origin',
        headers: {
            'Content-type': "application/json"
        },
        body: JSON.stringify({
            journalpostId: id,
            status,
            distribusjonstype
        })
    });
    if (response.status !== 200) {
        setStatus(STATUS_SUBMITTED)
        const body = await response.json()
        setErrorMessage("Feil ved distribuering journalpost. Melding: " + body.message)
    } else {
        setStatus(STATUS_SUBMITTED)
        setErrorMessage(undefined)
    }
}

async function hentJournalpostInfo(id, setStatus, setErrorMessage, setIsFritekst) {
    console.log('henter journalpostinfo for journalpost', id);
    const response = await fetch(`/api/journalpost/${id}`, {
        method: 'GET',
        credentials: 'same-origin',
    })
    const body = await response.json()
    if (response.status !== 200) {
        setErrorMessage("Feil ved henting av journalpostinfo. Melding: " + body.message)
    } else {
        setStatus(STATUS_READY_FOR_INPUT)
        setIsFritekst(body.fritekst)
    }
}

function Journalpost() {
    const {id} = useParams();
    const [status, setStatus] = useState(STATUS_INITIAL);
    const [isFritekst, setIsFritekst] = useState(false);
    const [errorMessage, setErrorMessage] = useState(undefined);
    const [distribusjonstype, setDistribusjonstype] = useState(() => {
        hentJournalpostInfo(id, setStatus, setErrorMessage, setIsFritekst)
    });


    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    const journalStatus = queryParams.get('status');

    const handleSubmit = async () => {
        if (distribusjonstype === undefined && isFritekst) {
            setErrorMessage("Varslingsmetode må fylles inn")
        } else {
            distribuerJournalpost({
                id,
                status: journalStatus,
                distribusjonstype: distribusjonstype
            }, setStatus, setErrorMessage);
        }
    };

    const inProgress = status === STATUS_SUBMITTING;
    const label = inProgress ? 'Send brevet...' : 'Send brevet';
    const shuldShowInput = status === STATUS_READY_FOR_INPUT || inProgress;
    const showVarslingsmetode = isFritekst && shuldShowInput;
    const isSuccess = status === STATUS_SUBMITTED && errorMessage === undefined;
    return (
        <div>
            <Heading size="large">Journalpost med ID: {id}</Heading>
            {showVarslingsmetode &&
                <div>
                    <Heading size="medium" style={{marginTop: '30px'}}>Du må velge varseltype</Heading>
                    <BodyLong size="medium" style={{marginTop: '15px'}}>Du har valgt å bruke et fritekstbrev. Da må du
                        alltid velge varseltype før brevet sendes. Når du har klikket på «send brevet», blir det bestemt
                        av en annen tjeneste om brevet skal sendes til bruker digitalt eller på papir.</BodyLong>
                    <ReadMore style={{marginTop: '30px'}} header="Slik skal de forskjellige varslingstypene brukes">
                        <div>
                            <Label size="medium">
                                Vedtak
                            </Label>
                            <BodyShort>
                                Bruk alltid denne ved vedtak.
                            </BodyShort>

                            <Label style={{marginTop: '30px'}} size="medium">
                                Viktig
                            </Label>
                            <BodyShort>
                                Bruk denne når det er viktig å sikre at bruker åpner brevet. Dette gjelder bla. a.
                                forhåndsvarslinger og meldinger som har betydning for brukers rettsstilling eller
                                behandling av sak.
                            </BodyShort>

                            <Label style={{marginTop: '30px'}} size="medium">
                                Annet
                            </Label>
                            <BodyShort>
                                Bruk denne når du ikke kan bruke «vedtak» eller «viktig».
                            </BodyShort>
                            <BodyShort style={{marginTop: '30px'}}>
                                Du finner mer informasjon om varseltypene i <a
                                target="_blank"
                                href="https://navno.sharepoint.com/sites/fag-og-ytelser-fagsystemer/SitePages/Sentral-print-distribusjon-av-Pesys-brev-p%C3%A5-gammel-brevl%C3%B8sning.aspx"
                            >
                                dette rutinedokumentet
                            </a>.
                            </BodyShort>
                        </div>
                    </ReadMore>
                    <RadioGroup onChange={(v) => setDistribusjonstype(v)}
                                disabled={inProgress}
                                style={{marginTop: '30px'}}
                                legend="Velg varseltype:" size="medium">
                        <Radio value="VEDTAK">Vedtak</Radio>

                        <Radio value="VIKTIG">Viktig</Radio>

                        <Radio value="ANNET">Annet</Radio>
                    </RadioGroup>
                </div>
            }

            {errorMessage && <Alert variant="error">{errorMessage}</Alert>}
            {isSuccess && <Alert variant="success">Journalpost er nå sendt.</Alert>}
            {shuldShowInput &&
                <Button onClick={handleSubmit}
                        disabled={isSuccess}
                        loading={inProgress}
                        style={{marginTop: '30px'}}>{label}</Button>}
            {status === STATUS_INITIAL && errorMessage === undefined &&
                <Loader variant="neutral" size="3xlarge" title="venter..."/>}
        </div>
    );
}


export default Journalpost;
