package com.github.cwilper.dsogen;

import com.google.common.base.Throwables;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An immutable list of unique words.
 */
public class WordList extends ForwardingList<String>
{
    private final List<String> delegate;

    private final Iterator<Integer> randomInts;

    // if seed is nonzero, construct deterministic "random" sequence using that seed, else use an entropy source
    public WordList(final long seed, final Iterable<String> words) {
        checkNotNull(words);
        if (words instanceof Set) {
            delegate = initDelegate((Set<String>) words);
        } else {
            delegate = initDelegate(Sets.newHashSet(words));
        }
        if (seed == 0) {
            randomInts = new Random().ints(0, size()).iterator();
        } else {
            randomInts = new Random(seed).ints(0, size()).iterator();
        }
    }

    public static WordList fromStream(final long seed, InputStream inputStream) {
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            final List<String> words = Lists.newArrayList();
            String line = reader.readLine();
            while (line != null) {
                words.add(line.trim());
                line = reader.readLine();
            }
            return new WordList(seed, words);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }
    }

    private List<String> initDelegate(Set<String> words) {
        List<String> sortedList = Lists.newArrayList(words);
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    @Override
    protected List<String> delegate() {
        return delegate;
    }

    public String random() {
        return get(randomInts.next());
    }
}
