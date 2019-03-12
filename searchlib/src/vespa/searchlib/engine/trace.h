// Copyright 2019 Oath Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#pragma once

#include <vespa/vespalib/stllike/string.h>
#include <vespa/fastos/timestamp.h>

namespace vespalib { class Slime; }
namespace vespalib::slime { class Cursor; }

namespace search::engine {

class Clock {
public:
    virtual ~Clock() = default;
    virtual fastos::TimeStamp now() const = 0;
};

class FastosClock : public Clock {
public:
    fastos::TimeStamp now() const override { return fastos::ClockSystem::now(); }
};

class CountingClock : public Clock {
public:
    CountingClock(int64_t start, int64_t increment) : _increment(increment), _nextTime(start) { }
    fastos::TimeStamp now() const override {
        int64_t prev = _nextTime;
        _nextTime += _increment;
        return prev;
    }
private:
    const int64_t   _increment;
    mutable int64_t _nextTime;
};

class RelativeTime {
public:
    RelativeTime(std::unique_ptr<Clock> clock);
    fastos::TimeStamp timeOfDawn() const { return _start; }
    fastos::TimeStamp timeSinceDawn() const { return _clock->now() - _start; }
    fastos::TimeStamp now() const { return _clock->now(); }
private:
    fastos::TimeStamp      _start;
    std::unique_ptr<Clock> _clock;
};

/**
 * Used for adding traces to a request. Acquire a new Cursor for everytime you want to trace something.
 * Note that it is not thread safe. All use of any cursor aquired must be thread safe.
 */
class Trace
{
public:
    using Cursor = vespalib::slime::Cursor;
    Trace(const RelativeTime & relativeTime, uint32_t traceLevel=0);
    ~Trace();

    /**
     * Will give you a trace entry. It will also add a timestamp relative to the creation of the trace.
     * @param name
     * @return a Cursor to use for further tracing.
     */
    Cursor & createCursor(vespalib::stringref name);
    Cursor * maybeCreateCursor(uint32_t level, vespalib::stringref name);
    /**
     * Will add a simple 'event' string. It will also add a timestamp relative to the creation of the trace.
     * @param level require for actually add the trace.
     * @param event
     */
    void addEvent(uint32_t level, vespalib::stringref event);

    /**
     * Will compute and and a final duration timing.
     */
    void done();

    vespalib::string toString() const;
    Cursor & getRoot() const { return _root; }
    vespalib::Slime & getSlime() const { return *_trace; }
    bool shouldTrace(uint32_t level) const { return level <= _level; }
    uint32_t getLevel() const { return _level; }
    Trace & setLevel(uint32_t level) { _level = level; return *this; }
    const RelativeTime & getRelativeTime() const { return _relativeTime; }
private:
    void addTimeStamp(Cursor & trace);
    std::unique_ptr<vespalib::Slime> _trace;
    Cursor              & _root;
    Cursor              & _traces;
    const RelativeTime  & _relativeTime;
    uint32_t              _level;
};

}