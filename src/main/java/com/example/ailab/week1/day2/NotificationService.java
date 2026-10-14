package com.example.ailab.week1.day2;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Singleton. Injecting a prototype {@link NotificationDraft} directly would
 * capture one instance when this singleton is created, and every call would
 * reuse it. {@link ObjectProvider#getObject()} asks the container again.
 */
@Service
public class NotificationService {

    private final Notifier notifier;
    private final ObjectProvider<NotificationDraft> drafts;

    public NotificationService(Notifier notifier, ObjectProvider<NotificationDraft> drafts) {
        this.notifier = notifier;
        this.drafts = drafts;
    }

    public NotificationReceipt notify(String message) {
        NotificationDraft draft = drafts.getObject();
        return new NotificationReceipt(notifier.deliver(message), draft.id());
    }

    public record NotificationReceipt(String delivery, String draftId) {
    }
}
