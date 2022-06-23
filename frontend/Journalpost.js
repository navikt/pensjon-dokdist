import React, {useReducer} from "react";
import {useLocation, useParams} from "react-router-dom";
import {Alert, Button, Heading, Radio, RadioGroup} from "@navikt/ds-react";
import {fetch} from "whatwg-fetch";
import NoPage from "./NoPage";

async function distribuerJournalpost({id, status}) {
    console.log('distribuer', id);
    console.log('status', status);
    const response = await fetch(`/api/journalpost/${id}/send`, {
        method: 'POST',
        credentials: 'same-origin',
        headers: {
            'Content-type': "application/json"
        },
        body: JSON.stringify({
            journalpostId: id,
            status
        })
    });

    if (response.status !== 200) {
        throw new Error('Noe gikk galt');
    }
}

const initialState = {
    status: 'INITIAL'
};

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
        case 'SUBMIT_ERROR':
            return {
                ...state,
                status: 'SUBMIT_ERROR'
            };
        default:
            console.log('Unknown type', action);
            return state;
    }
}

function Journalpost() {
    const [state, dispatch] = useReducer(reducer, initialState);
    const location = useLocation();

    const queryParams = new URLSearchParams(location.search);
    const status = queryParams.get('status');

    const {id} = useParams();

    const handleSubmit = async () => {
        try {
            dispatch({type: 'SUBMIT'});
            await distribuerJournalpost({id, status});
            dispatch({type: 'SUBMIT_SUCCESS'});
        } catch (err) {
            dispatch({type: 'SUBMIT_ERROR'});
        }
    };

    const inProgress = state.status === 'SUBMITTING';
    const label = inProgress ? 'Send brevet...' : 'Send brevet';
    const shouldShowButton = state.status === 'INITIAL' || inProgress;
    const isSuccess = state.status === 'SUBMITTED';
    const isError = state.status === 'SUBMIT_ERROR';

    return (
        <div>
            <Heading size={"large"}>Journalpost med ID: {id}</Heading>
            {shouldShowButton &&
                <Button onClick={handleSubmit} disabled={inProgress} spinner={inProgress}>{label}</Button>}
            {isSuccess && <Alert variant="success">Journalpost er nå sendt.</Alert>}
            {isError && <Alert variant="error">Noe gikk visst galt!</Alert>}
            <RadioGroup legend="Velg din aldersgruppe."
                size="medium">
                <Radio value="10">10-20 år</Radio>
                <Radio value="20">21-45 år</Radio>
                <Radio value="40">46-80 år</Radio>
            </RadioGroup>
        </div>
    );
}


export default Journalpost;
