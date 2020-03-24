package no.nav.pensjondokdist.journalforing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import no.nav.pensjondokdist.journalforing.dto.FerdigstillJournalpostRequest;
import no.nav.pensjondokdist.saf.SafService;
import no.nav.pensjondokdist.sts.StsService;

@RunWith(MockitoJUnitRunner.class)
public class JournalforingServiceTest {
    @Mock
    private SafService safService;
    @Mock
    private StsService stsService;
    @Mock
    private JournalforingClient client;

    private JournalforingService journalforingService;

    @Before
    public void init() {
        journalforingService = new JournalforingService(client, safService, stsService);
    }

    @Test
    public void testShouldReturnTrueIfJouranlpostStatusIsFL() {
        when(safService.hentJournalforendeEnhet("1234")).thenReturn("4833");
        when(stsService.getSystemBrukerToken()).thenReturn("token");
        when(client.execute(any(String.class), any(FerdigstillJournalpostRequest.class), any(String.class))).thenReturn(true);

        Boolean status = journalforingService.ferdigstillJournalpost("FL", "1234");

        assertThat(status).isTrue();
    }

    @Test
    public void testShouldReturnTrueIfJouranlpostStatusIsFS() {
        Boolean status = journalforingService.ferdigstillJournalpost("FS", "1234");

        assertThat(status).isTrue();
        verify(client, never()).execute(any(String.class), any(FerdigstillJournalpostRequest.class), any(String.class));
    }

    @Test
    public void testShouldReturnFalseIfJouranlpostStatusIsNotFSOrFL() {
        Boolean status = journalforingService.ferdigstillJournalpost("LL", "1234");

        assertThat(status).isFalse();
        verify(client, never()).execute(any(String.class), any(FerdigstillJournalpostRequest.class), any(String.class));
    }
}
