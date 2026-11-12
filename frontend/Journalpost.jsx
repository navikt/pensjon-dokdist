import React, {useState} from "react";
import {useLocation, useParams} from "react-router";
import {
    Alert,
    BodyLong,
    BodyShort,
    Box,
    Button,
    CopyButton,
    Heading,
    HGrid,
    Label,
    Loader,
    Radio,
    RadioGroup,
    ReadMore,
    VStack
} from "@navikt/ds-react";
import {FilesIcon} from "@navikt/aksel-icons";
import "@navikt/ds-css/dist/index.min.css";
import './Journalpost.css';


const STATUS_INITIAL = 'INITIAL';
const STATUS_READY_FOR_INPUT = 'READY_FOR_INPUT';
const STATUS_SUBMITTING = 'SUBMITTING';
const STATUS_SUBMITTED = 'SUBMITTED';
const STATUS_ALREADY_DISTRIBUTED = 'ALREADY_DISTRIBUTED';
const STATUS_MOTTAKER_DOED = 'MOTTAKER_DOED';
const STATUS_DISTRIBUTION_FAILED = 'DISTRIBUTION_FAILED';
const STATUS_INFO_FAILED = 'INFO_FAILED';
const CALL_ID_HEADER = 'Nav-Call-Id';

function KorrelasjonsId({callId}) {
    if (!callId) return null;
    return (
        <VStack gap="space-4">
            <BodyShort size="small">
                Hvis det skjer igjen, trykk på knappen <i>Kopier ID</i> nedenfor og meld feil til oss i Porten.
            </BodyShort>
            <Box asChild background="default" borderColor="neutral" borderRadius="4" borderWidth="1">
                <HGrid align="center" columns="auto max-content" gap="space-8" paddingInline="space-8 space-0" style={{width: 'fit-content', maxWidth: '100%'}}>
                    <BodyShort truncate size="small" data-testid="korrelasjons-id">{callId}</BodyShort>
                    <Box asChild borderRadius="4">
                        <CopyButton
                            copyText={callId}
                            data-color="accent"
                            icon={<FilesIcon aria-hidden/>}
                            size="small"
                            text="Kopier ID"
                            activeText="Kopiert"
                            style={{margin: 0}}
                        />
                    </Box>
                </HGrid>
            </Box>
        </VStack>
    );
}

async function distribuerJournalpost({id, status, distribusjonstype}, setStatus, setErrorMessage, setCallId, isRetry = false) {
    console.log('distribuer', id);
    console.log('status', status);
    const callId = crypto.randomUUID()
    if (!isRetry) {
        setStatus(STATUS_SUBMITTING)
        setErrorMessage(undefined)
        setCallId(callId)
    }

    // Om sesjonen til bruker har gått ut i backend så vil backend svare med 302 redirect til login og fetch vil automatisk følge denne, men login krever interaksjon fra bruker og kan ikke gjøres med XHR-request.
    // redirect: "error" fører til at fetch kaster en exception vi kan fange ved 302.
    // catch(() => window.location.reload()) vil refreshe siden og da vil backend redirecte til login og bruker får logget inn
    const response = await fetch(`/api/journalpost/${id}/send`, {
        method: 'POST',
        credentials: 'same-origin',
        headers: {
            'Content-type': "application/json",
            [CALL_ID_HEADER]: callId,
        },
        redirect: "error",
        body: JSON.stringify({
            journalpostId: id,
            status,
            distribusjonstype
        })
    }).catch(() => window.location.reload());
    setCallId(callId)

    // Om svaret er 401 så betyr det (mest sannsynlig) at accessTokenet i brukerens sesjon er utløpt, vi logger ut og refresher for ny login for å komme oss rundt det.
    if(response.status === 401) {
        await fetch("/logout")
        window.location.reload()
    } else if (response.status === 409) {
        setStatus(STATUS_ALREADY_DISTRIBUTED)
        setErrorMessage(undefined)
    } else if (response.status === 410) {
        setStatus(STATUS_MOTTAKER_DOED)
        setErrorMessage(undefined)
    } else if (response.status !== 200 && response.status !== 204) {
        const body = await response.json().catch(() => ({}))
        setStatus(STATUS_DISTRIBUTION_FAILED)
        setErrorMessage(body.message || `Ukjent feil (HTTP ${response.status})`)
    } else {
        setStatus(STATUS_SUBMITTED)
        setErrorMessage(undefined)
    }
}

async function hentJournalpostInfo(id, setStatus, setErrorMessage, setIsFritekst, setCallId) {
    console.log('henter journalpostinfo for journalpost', id);
    const callId = crypto.randomUUID()

    // Om sesjonen til bruker har gått ut i backend så vil backend svare med 302 redirect til login og fetch vil automatisk følge denne, men login krever interaksjon fra bruker og kan ikke gjøres med XHR-request.
    // redirect: "error" fører til at fetch kaster en exception vi kan fange ved 302.
    // catch(() => window.location.reload()) vil refreshe siden og da vil backend redirecte til login og bruker får logget inn
    const response = await fetch(`/api/journalpost/${id}`, {
        method: 'GET',
        credentials: 'same-origin',
        headers: {
            [CALL_ID_HEADER]: callId,
        },
        redirect: "error",
    }).catch(() => window.location.reload());

    const body = await response.json().catch(() => ({}))
    // Om svaret er 401 så betyr det (mest sannsynlig) at accessTokenet i brukerens sesjon er utløpt, vi logger ut og refresher for ny login for å komme oss rundt det.
    if(response.status === 401) {
        await fetch("/logout")
        window.location.reload()
    } else if (response.status !== 200) {
        setCallId(callId)
        setStatus(STATUS_INFO_FAILED)
        setErrorMessage(body.message || `Ukjent feil (HTTP ${response.status})`)
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
    const [callId, setCallId] = useState(undefined);
    const [isRetrying, setIsRetrying] = useState(false);
    const [distribusjonstype, setDistribusjonstype] = useState(() => {
        hentJournalpostInfo(id, setStatus, setErrorMessage, setIsFritekst, setCallId)
    });


    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    const journalStatus = queryParams.get('status');

    const handleSubmit = async () => {
        if (distribusjonstype === undefined && isFritekst) {
            setCallId(undefined)
            setErrorMessage("Varslingsmetode må fylles inn")
        } else {
            distribuerJournalpost({
                id,
                status: journalStatus,
                distribusjonstype: distribusjonstype
            }, setStatus, setErrorMessage, setCallId);
        }
    };

    const handleRetry = async () => {
        setIsRetrying(true)
        try {
            await distribuerJournalpost({
                id,
                status: journalStatus,
                distribusjonstype: distribusjonstype
            }, setStatus, setErrorMessage, setCallId, true);
        } finally {
            setIsRetrying(false)
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

            {status === STATUS_DISTRIBUTION_FAILED &&
                <Alert variant="error" size="small" style={{overflowWrap: 'anywhere'}}>
                    <VStack gap="space-12" align="start">
                        <VStack gap="space-8">
                            <Heading size="small" level="2">Brevet er ikke sendt</Heading>
                            <BodyShort size="small">Det oppstod en feil, og brevet ble ikke sendt. Du kan prøve å sende brevet på nytt.</BodyShort>
                            <BodyShort size="small">Feilmelding: {errorMessage}</BodyShort>
                            <KorrelasjonsId callId={callId}/>
                        </VStack>
                        <Button size="small" type="button" data-color="accent" loading={isRetrying} onClick={handleRetry} style={{margin: 0}}>
                            Prøv å sende igjen
                        </Button>
                    </VStack>
                </Alert>}
            {status === STATUS_INFO_FAILED &&
                <Alert variant="error" size="small" style={{overflowWrap: 'anywhere'}}>
                    <VStack gap="space-8">
                        <Heading size="small" level="2">Kunne ikke hente journalposten</Heading>
                        <BodyShort size="small">Feilmelding: {errorMessage}</BodyShort>
                        <KorrelasjonsId callId={callId}/>
                    </VStack>
                </Alert>}
            {errorMessage && status !== STATUS_DISTRIBUTION_FAILED && status !== STATUS_INFO_FAILED &&
                <Alert variant="error" size="small">{errorMessage}</Alert>}
            {isSuccess && <Alert variant="success">Journalpost er nå sendt.</Alert>}
            {status === STATUS_ALREADY_DISTRIBUTED && <Alert variant="info">Journalposten er allerede distribuert.</Alert>}
            {status === STATUS_MOTTAKER_DOED &&
                <Alert variant="warning" size="small">
                    <VStack gap="space-8">
                        <Heading size="small" level="2">Brevet er ikke sendt</Heading>
                        <BodyShort size="small">Mottakeren er død, så brevet kan ikke distribueres.</BodyShort>
                    </VStack>
                </Alert>}
            {shuldShowInput &&
                <Button onClick={handleSubmit}
                        disabled={isSuccess}
                        loading={inProgress}
                        style={{marginTop: '30px'}}>{label}</Button>}
            {status === STATUS_INITIAL &&
                <Loader variant="neutral" size="3xlarge" title="venter..."/>}
        </div>
    );
}


export default Journalpost;
