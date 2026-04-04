// TRIá»‚N KHAI THUáº¬T TOÃN CHáº¤M CHÃNH Táº¢ THEO Tá»ªNG Tá»ª.
// - So sÃ¡nh tá»«ng tá»« ngÆ°á»i dÃ¹ng gÃµ vá»›i Ä‘Ã¡p Ã¡n trong Database.
// - Bá»Ž QUA dáº¥u cÃ¢u (.,?!;:) khi so sÃ¡nh nhÆ°ng GIá»® NGUYÃŠN khi hiá»ƒn thá»‹.
// - Táº¡o máº£ng gá»£i Ã½: tá»« Ä‘Ãºng giá»¯ nguyÃªn, tá»« chÆ°a Ä‘Ãºng thÃ nh "***".
// - ÄÃ¡nh dáº¥u chá»‰ sá»‘ tá»« Má»šI Ä‘Æ°á»£c gá»£i Ã½ (newHintIndex) Ä‘á»ƒ Frontend tÃ´ xanh.

package com.english.learning.service.impl.learning.dictation;

import com.english.learning.dto.DictationResultDTO;
import com.english.learning.entity.Sentence;
import com.english.learning.exception.SentenceNotFoundException;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.service.learning.dictation.DictationService;
import com.english.learning.util.TextNormalizerUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DictationServiceImpl implements DictationService {

    private final SentenceRepository sentenceRepository;

    public DictationServiceImpl(SentenceRepository sentenceRepository) {
        this.sentenceRepository = sentenceRepository;
    }

    /**
     * THUáº¬T TOÃN CHÃNH: Cháº¥m Ä‘Ã¡p Ã¡n theo tá»«ng tá»«.
     *
     * Luáº­t:
     * - So sÃ¡nh khÃ´ng phÃ¢n biá»‡t hoa thÆ°á»ng, bá» qua dáº¥u cÃ¢u.
     * - Äáº¿m sá»‘ tá»« Ä‘Ãºng liÃªn tiáº¿p tá»« Ä‘áº§u (matchedCount).
     * - Gá»£i Ã½: hiá»‡n (matchedCount + 1) tá»« gá»‘c, áº©n pháº§n cÃ²n láº¡i.
     * - Tá»« thá»© (matchedCount) lÃ  tá»« Má»šI Ä‘Æ°á»£c gá»£i Ã½ (tÃ´ xanh).
     */
    @Override
    public DictationResultDTO checkAnswer(Long sentenceId, String userInput) {
        Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new SentenceNotFoundException(sentenceId));

        String correctContent = sentence.getContent();

        // TÃ¡ch tá»« (giá»¯ nguyÃªn dáº¥u cÃ¢u gá»‘c Ä‘á»ƒ hiá»ƒn thá»‹)
        String[] correctWords = correctContent.trim().split("\\s+");
        String[] userWords = userInput.trim().split("\\s+");

        // Äáº¿m sá»‘ tá»« Ä‘Ãºng liÃªn tiáº¿p (bá» qua dáº¥u cÃ¢u khi so sÃ¡nh)
        int matchedCount = 0;
        for (int i = 0; i < Math.min(correctWords.length, userWords.length); i++) {
            if (TextNormalizerUtil.removePunctuationAndLowercase(correctWords[i])
                    .equals(TextNormalizerUtil.removePunctuationAndLowercase(userWords[i]))) {
                matchedCount++;
            } else {
                break;
            }
        }

        boolean isCorrect = (matchedCount == correctWords.length);

        // Táº¡o máº£ng hint
        List<String> hintWords = new ArrayList<>();
        int newHintIndex = -1;

        if (isCorrect) {
            // ÄÃºng háº¿t: hiá»‡n toÃ n bá»™ cÃ¢u gá»‘c
            for (String w : correctWords) {
                hintWords.add(w);
            }
        } else {
            // Sá»‘ tá»« hiá»‡n ra = matchedCount + 1
            int revealCount = Math.min(matchedCount + 1, correctWords.length);
            newHintIndex = matchedCount; // Tá»« má»›i gá»£i Ã½ = vá»‹ trÃ­ ngay sau pháº§n Ä‘Ãºng

            for (int i = 0; i < correctWords.length; i++) {
                if (i < revealCount) {
                    hintWords.add(correctWords[i]); // Hiá»‡n tá»« gá»‘c (giá»¯ nguyÃªn hoa/thÆ°á»ng + dáº¥u cÃ¢u)
                } else {
                    hintWords.add("***");
                }
            }
        }

        return new DictationResultDTO(
                isCorrect,
                matchedCount,
                correctWords.length,
                hintWords,
                newHintIndex,
                isCorrect ? correctContent : null
        );
    }

    /**
     * Chá»©c nÄƒng Skip: Tráº£ vá» toÃ n bá»™ Ä‘Ã¡p Ã¡n.
     */
    @Override
    public DictationResultDTO skipSentence(Long sentenceId) {
        Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new SentenceNotFoundException(sentenceId));

        String correctContent = sentence.getContent();
        String[] correctWords = correctContent.trim().split("\\s+");

        List<String> hintWords = new ArrayList<>();
        for (String w : correctWords) {
            hintWords.add(w);
        }

        return new DictationResultDTO(
                false,
                0,
                correctWords.length,
                hintWords,
                -1,
                correctContent
        );
    }
}

