import React, {useEffect, useReducer} from "react";
import {useLocation, useParams} from "react-router-dom";
import {Alert, BodyLong, Button, Heading, Loader, Radio, RadioGroup} from "@navikt/ds-react";
import "@navikt/ds-css";
import {fetch} from "whatwg-fetch";
import './Journalpost.css';


async function distribuerJournalpost({id, status, distribusjonstype}, dispatch) {
    console.log('distribuer', id);
    console.log('status', status);
    dispatch({type: 'SUBMIT'});
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
    const body = await response.json()

    if (response.status !== 200) {
        dispatch({type: 'SUBMIT_ERROR', errorMessage: "Feil ved distribuering journalpost. Melding: " + body.message});
    } else {
        dispatch({type: 'SUBMIT_SUCCESS'});
    }
}

async function hentJournalpostInfo(id, dispatch) {
    console.log('henter journalpostinfo for journalpost', id);
    dispatch({type: 'HENT_JOURNALPOSTINFO'})
    const response = await fetch(`/api/journalpost/${id}`, {
        method: 'GET',
        credentials: 'same-origin',
    })
    const body = await response.json()
    if (response.status !== 200) {
        dispatch({type: 'HENT_JOURNALPOSTINFO_ERROR', errorMessage: "Feil ved henting av journalpostinfo. Melding: " + body.message})
    } else {
        dispatch({type: 'HENT_JOURNALPOSTINFO_SUCCESS', data: body})
    }
}

function reducer(state, action) {
    console.log(action.type, action);
    switch (action.type) {
        case 'SUBMIT':
            return {
                ...state,
                status: 'SUBMITTING'
            };
        case 'SUBMIT_SUCCESS':
            return {
                ...state,
                status: 'SUBMITTED'
            };
        case 'HENT_JOURNALPOSTINFO':
            return {
                ...state,
                status: 'HENTER_JOURNALPOSTINFO'
            }
        case 'SUBMIT_ERROR':
        case 'HENT_JOURNALPOSTINFO_ERROR':
            return {
                ...state,
                errorMessage: action.errorMessage,
                status: 'READY_FOR_INPUT'
            }
        case 'HENT_JOURNALPOSTINFO_SUCCESS':
            return {
                ...state,
                isFritekst: action.data.fritekst,
                status: 'READY_FOR_INPUT',
                errorMessage: undefined,
            }
        case 'UPDATE_DISTRIBUSJONSTYPE':
            return {
                ...state,
                distribusjonstype: action.distribusjonstype,
            }
        default:
            console.log('Unknown type', action);
            return state;
    }
}

const initialState = {
    status: 'INITIAL',
    isFritekst: false,
    distribusjonstype: undefined,
    errorMessage: undefined,
};

function Journalpost() {
    const [state, dispatch] = useReducer(reducer, initialState);
    const {id} = useParams();

    useEffect(() => {
        if (state.status !== 'INITIAL') {
            return;
        }
        hentJournalpostInfo(id, dispatch);
    }, [state, dispatch]);

    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    const status = queryParams.get('status');

    const handleSubmit = async () => {
        if (state.distribusjonstype === undefined) {
            dispatch({type: 'SUBMIT_ERROR', errorMessage: "Varslingsmetode må fylles inn"});
        } else {
            distribuerJournalpost({id, status, distribusjonstype: state.distribusjonstype}, dispatch);
        }
    };

    const onChangeVarslingsmetode = async (distribusjonstype) => {
        dispatch({type: 'UPDATE_DISTRIBUSJONSTYPE', distribusjonstype})
    }

    const inProgress = state.status === 'SUBMITTING';
    const label = inProgress ? 'Send brevet...' : 'Send brevet';
    const shouldShowButton = state.status === 'READY_FOR_INPUT' || inProgress;
    const showVarslingsmetode = (state.status === 'READY_FOR_INPUT' || inProgress) && state.isFritekst;
    const isSuccess = state.status === 'SUBMITTED';
    return (
        <div>
            <Heading spacing size={"large"}>Journalpost med ID: {id}</Heading>
            {showVarslingsmetode &&
                <div>
                    <BodyLong spacing>Du har valgt å bruke fritekstmalen «Brev fra NAV». Da må du alltid velge varseltype før brevet sendes. Etter du har trykker på «Send brevet» blir det bestemt av en tjeneste utenfor Pesys, om brevet skal distribueres digitalt til bruker eller sendes ut på papir.</BodyLong>
                    <RadioGroup onChange={(v) => onChangeVarslingsmetode(v)} disabled={inProgress}
                                legend="Velg varseltype:" size="medium">
                        <Radio value="VEDTAK">
                            <Heading size="small" level="2">Vedtak</Heading>
                            <BodyLong>Bruk alltid denne ved vedtak.</BodyLong>
                        </Radio>
                        <Radio value="VIKTIG">
                            <Heading size="small" level="2">Viktig</Heading>
                            <BodyLong>Bruk denne når det er av betydning å sikre at bruker åpner brevet. Dette gjelder forhåndsvarsel og andre meldinger som har betydning for brukers rettsstilling eller behandling av saken. Det samme gjelder meldinger som av andre grunner er viktig at bruker mottar.</BodyLong>
                        </Radio>
                        <Radio value="ANNET">
                            <Heading size="small" level="2">Annet</Heading>
                            <BodyLong>Bruk denne ved forsendelse der varseltype for «Vedtak» eller «Viktig» ikke kan benyttes.</BodyLong>
                        </Radio>
                        <BodyLong spacing>Du finner mer informasjon om de ulike varslene i rutinen «Sentral print/distribusjon av Pesys-brev på gammel brevløsning» på fagsystemsidene for Pesys på Navet.</BodyLong>
                    </RadioGroup>
                </div>
            }
            {shouldShowButton &&
                <Button onClick={handleSubmit} disabled={inProgress} loading={inProgress}>{label}</Button>}
            {state.status === 'HENTER_JOURNALPOSTINFO' && <Loader variant="neutral" size="3xlarge" title="venter..."/>}
            {isSuccess && <Alert variant="success">Journalpost er nå sendt.</Alert>}
            {state.errorMessage && <Alert variant="error">{state.errorMessage}</Alert>}
        </div>
    );
}


export default Journalpost;
