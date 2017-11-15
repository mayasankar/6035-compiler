//===========================================================================
//
//   FILE: 6035.c:
//   
//   Author: Sam Larsen
//   Date: Mon Dec  6 23:43:53 2004
//
//   Function:  A few external functions we can use to read and write
//		black & white pgm images in a Decaf program.  This is
//		quick and dirty -- there's little error checking and 
//		input files must conform to a strict format.
//
//   Modifications by Michael Gordon in Fall 2006
//              Added benchmarking functionality
//              Added data-parallelization library 
//
//   Modifications by Michal Karczmarek in Fall 2007
//              Changed names of globals to make sure they are all unique
//              Added color ppm support.
//
//===========================================================================

#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>
//#include "papiex.h"

static FILE* fp_6035 = NULL;
static int cols_6035 = 0;
static int rows_6035 = 0;
static struct timeval* t1_6035;
static struct timeval* t2_6035;
static struct timeval* diff_6035;

static int _NUM_THREADS_6035;

int timeval_subtract (result, x, y)
     struct timeval *result, *x, *y;
{
  /* Perform the carry for the later subtraction by updating y. */
  if (x->tv_usec < y->tv_usec) {
    int nsec = (y->tv_usec - x->tv_usec) / 1000000 + 1;
    y->tv_usec -= 1000000 * nsec;
    y->tv_sec += nsec;
  }
  if (x->tv_usec - y->tv_usec > 1000000) {
    int nsec = (x->tv_usec - y->tv_usec) / 1000000;
    y->tv_usec += 1000000 * nsec;
    y->tv_sec -= nsec;
  }

  /* Compute the time remaining to wait.
     tv_usec is certainly positive. */
  result->tv_sec = x->tv_sec - y->tv_sec;
  result->tv_usec = x->tv_usec - y->tv_usec;

  /* Return 1 if result is negative. */
  return x->tv_sec < y->tv_sec;
}

void start_caliper() {
  t1_6035 = (struct timeval *)malloc(sizeof(struct timeval));
  t2_6035 = (struct timeval *)malloc(sizeof(struct timeval));
  diff_6035 = (struct timeval *)malloc(sizeof(struct timeval));
  gettimeofday(t1_6035, NULL);
}

void end_caliper() {

  gettimeofday(t2_6035, NULL);
  
  timeval_subtract(diff_6035, t2_6035, t1_6035);
  
  printf("Timer: %d usecs\n", ((1000000 * diff_6035->tv_sec) + diff_6035->tv_usec));
}

void set_num_threads(int i) {
  _NUM_THREADS_6035 = i;
}

void create_and_run_threads(void* (*f)(void *) ) {
  pthread_t thread[_NUM_THREADS_6035];
  pthread_attr_t attr;
  int i, rc, status;

  //set the status of each thread created as joinable
  pthread_attr_init(&attr);
  pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);

  // create the threads and pass to each instance its ID
  // note that 0th thread is called from *this* thread
  // without spawning an extra thread!
  for (i = 1; i < _NUM_THREADS_6035; i++) {
    //create the thread id passing/sharing structure

    rc = pthread_create(&thread[i], &attr, f, (void*)i);
    if (rc) {
      printf("ERROR: cannot create thread!\n");
      exit(-1);
    }
  }

  // call for the 0th thread
  f ((void*)0);

  //join the threads
  for (i = 1; i < _NUM_THREADS_6035; i++) {
    rc = pthread_join(thread[i], (void**)&status);
    if (rc) {
      printf("ERROR: cannot join thread! \n");
      exit(-1);
    }
  }
}

void pgm_open_for_read(char* name)
{
  int n;
  fp_6035 = fopen(name, "r");
  if (fp_6035 == NULL)
  {
    fprintf(stderr, "error opening %s\n", name);
    exit(1);
  }
  fscanf(fp_6035, "P2%d%d%d", &cols_6035, &rows_6035, &n);
}

u_int64_t pgm_get_cols()
{
  return (u_int64_t)cols_6035;
}

u_int64_t pgm_get_rows()
{
  return (u_int64_t)rows_6035;
}

u_int64_t pgm_get_next_pixel()
{
  int p;
  fscanf(fp_6035, "%d", &p);
  return (u_int64_t)p;
}

void pgm_close()
{
  fclose(fp_6035);
  fp_6035 = NULL;
}

void pgm_open_for_write(char* name, int c, int r)
{
  fp_6035 = fopen(name, "w");
  if (fp_6035 == NULL)
  {
    fprintf(stderr, "ERROR opening %s\n", name);
    exit(1);
  }
  fprintf(fp_6035, "P2\n%d %d\n255\n", c, r);
}

void pgm_write_next_pixel(int p)
{
  fprintf(fp_6035, "%d ", p);
}

void ppm_open_for_read(char* name)
{
  int n;
  fp_6035 = fopen(name, "r");
  if (fp_6035 == NULL)
  {
    fprintf(stderr, "error opening %s\n", name);
    exit(1);
  }
  fscanf(fp_6035, "P3%d%d%d", &cols_6035, &rows_6035, &n);
}

u_int64_t ppm_get_cols()
{
  return (u_int64_t)cols_6035;
}

u_int64_t ppm_get_rows()
{
  return (u_int64_t)rows_6035;
}

u_int64_t ppm_get_next_pixel_color()
{
  int p;
  fscanf(fp_6035, "%d", &p);
  return (u_int64_t)p;
}

void ppm_close()
{
  fclose(fp_6035);
  fp_6035 = NULL;
}

void ppm_open_for_write(char* name, int c, int r)
{
  fp_6035 = fopen(name, "w");
  if (fp_6035 == NULL)
  {
    fprintf(stderr, "ERROR opening %s\n", name);
    exit(1);
  }
  fprintf(fp_6035, "P3\n%d %d\n255\n", c, r);
}

void ppm_write_next_pixel(int r, int g, int b)
{
  fprintf(fp_6035, "%d %d %d ", r, g, b);
}

